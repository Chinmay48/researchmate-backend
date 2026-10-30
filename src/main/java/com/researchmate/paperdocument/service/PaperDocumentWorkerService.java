package com.researchmate.paperdocument.service;

import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.entity.PaperProcessingStatus;
import com.researchmate.paperdocument.repository.PaperDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "researchmate.documents.worker-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class PaperDocumentWorkerService {

    private final PaperDocumentRepository documentRepository;
    private final PdfDownloadService downloadService;
    private final PdfTextExtractionService extractionService;
    private final PaperChunkingService chunkingService;
    private final PaperChunkPersistenceService persistenceService;
    private final PaperDocumentClaimService claimService;
    private final PaperDocumentCompletionService completionService;
    @Value("${researchmate.documents.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${researchmate.documents.poll-delay-ms:5000}")
    public void processPendingDocuments() {

        int recovered = claimService.recoverStaleDocuments();

        if (recovered > 0) {
            log.warn("Recovered {} stale paper processing jobs", recovered);
        }

        Optional<PaperDocument> pendingDocument =
                documentRepository.findFirstByStatusOrderByCreatedAtAsc(
                        PaperProcessingStatus.PENDING
                );

        pendingDocument.ifPresent(document -> {
            try {
                processDocument(document.getId());
            } catch (Exception ex) {
                log.error(
                        "Failed to process paper document {}",
                        document.getId(),
                        ex
                );
            }
        });
    }

    public void processDocument(UUID documentId) {
        if (!claimService.claimDocument(documentId)) {
            return;
        }

        Path pdfPath = null;

        try {
            PaperDocument document = documentRepository.findWithPaperById(documentId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Paper document not found: " + documentId
                    ));
            String pdfUrl = document.getPaper().getPdfUrl();

            if (pdfUrl == null || pdfUrl.isBlank()) {
                throw new IOException("Paper has no PDF URL");
            }

            pdfPath = downloadService.downloadPdf(pdfUrl);

            var pages = extractionService.extractPages(pdfPath);
            var chunks = chunkingService.chunkPages(pages);

            if (chunks.isEmpty()) {
                throw new IOException("No chunks were extracted");
            }

            int persistedCount = completionService.completeDocument(
                    documentId,
                    document,
                    chunks
            );

            log.info(
                    "Processed document {} with {} chunks",
                    documentId,
                    persistedCount
            );
        } catch (Exception exception) {
            String safeError = exception.getMessage();

            if (safeError == null || safeError.isBlank()) {
                safeError = "Document processing failed";
            }

            // Avoid storing excessively long error messages.
            if (safeError.length() > 1000) {
                safeError = safeError.substring(0, 1000);
            }

            try {
                claimService.recordFailure(
                        documentId,
                        maxAttempts,
                        safeError
                );
            } catch (Exception statusException) {
                log.error(
                        "Could not update failure status for document {}",
                        documentId,
                        statusException
                );
            }

            log.error(
                    "Document processing failed for {}",
                    documentId,
                    exception
            );

        } finally {
            if (pdfPath != null) {
                try {
                    Files.deleteIfExists(pdfPath);
                } catch (IOException cleanupException) {
                    log.warn(
                            "Could not delete temporary PDF {}",
                            pdfPath,
                            cleanupException
                    );
                }
            }
        }
    }


}