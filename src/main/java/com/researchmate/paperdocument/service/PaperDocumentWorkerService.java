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

    @Value("${researchmate.documents.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${researchmate.documents.poll-delay-ms:5000}")
    public void processPendingDocuments() {
        List<PaperDocument> pendingDocuments =
                documentRepository.findTop10ByStatusOrderByCreatedAtAsc(
                        PaperProcessingStatus.PENDING
                );

        for (PaperDocument document : pendingDocuments) {
            try {
                processDocument(document.getId());
            } catch (Exception exception) {
                log.error(
                        "Unexpected error processing document {}",
                        document.getId(),
                        exception
                );
            }
        }
    }

    public void processDocument(UUID documentId) {
        if (!claimDocument(documentId)) {
            return;
        }

        Path pdfPath = null;

        try {
            PaperDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() ->
                            new IllegalStateException("Document not found"));

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

            persistenceService.persistChunks(document, chunks);

            if (documentRepository.markCompleted(documentId) != 1) {
                throw new IllegalStateException(
                        "Could not mark document as completed"
                );
            }

            log.info(
                    "Processed document {} with {} chunks",
                    documentId,
                    chunks.size()
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
                documentRepository.recordFailure(
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

    @Transactional
    protected boolean claimDocument(UUID documentId) {
        return documentRepository.claimPendingDocument(documentId) == 1;
    }
}