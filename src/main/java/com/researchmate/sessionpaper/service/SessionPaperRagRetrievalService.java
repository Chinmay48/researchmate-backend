package com.researchmate.sessionpaper.service;

import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.projection.RetrievedChunk;
import com.researchmate.paperdocument.repository.PaperDocumentRepository;
import com.researchmate.paperdocument.service.PaperChunkRetrievalService;
import com.researchmate.paperdocument.service.PaperDocumentSchedulingService;
import com.researchmate.sessionpaper.dto.SessionRagChunkResponse;
import com.researchmate.sessionpaper.service.SessionPaperValidationService;
import com.researchmate.user.entity.User;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionPaperRagRetrievalService {

    private final SessionPaperValidationService validationService;
    private final PaperChunkRetrievalService retrievalService;
    private final PaperDocumentRepository paperDocumentRepository;
    private final PaperDocumentSchedulingService schedulingService;
    private final UserRepository userRepository;
    public List<SessionRagChunkResponse> retrieveSessionChunks(
            UUID sessionId,
            String email,
            String query,
            int topK
    ) {
        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();

        List<UUID> selectedPaperIds =
                validationService.getSelectedPaperIds(sessionId, userId);

        scheduleMissingDocuments(selectedPaperIds);

        validatePapersReady(selectedPaperIds);

        List<RetrievedChunk> chunks =
                retrievalService.retrieveRelevantChunks(
                        query,
                        selectedPaperIds,
                        topK
                );

        if (chunks.isEmpty()) {
            throw new IllegalStateException(
                    "No relevant chunks found in the selected papers"
            );
        }

        return mapToResponses(chunks);
    }

    private void validatePapersReady(List<UUID> paperIds) {

        List<PaperDocument> documents =
                paperDocumentRepository.findAllByPaper_IdIn(paperIds);

        Map<UUID, PaperDocument> documentMap = documents.stream()
                .collect(Collectors.toMap(
                        document -> document.getPaper().getId(),
                        Function.identity()
                ));

        List<UUID> missing = new ArrayList<>();
        List<UUID> pending = new ArrayList<>();
        List<UUID> processing = new ArrayList<>();
        List<UUID> failed = new ArrayList<>();

        for (UUID paperId : paperIds) {

            PaperDocument document = documentMap.get(paperId);

            if (document == null) {
                missing.add(paperId);
                continue;
            }

            switch (document.getStatus()) {
                case PENDING -> pending.add(paperId);
                case PROCESSING -> processing.add(paperId);
                case FAILED -> failed.add(paperId);
                case COMPLETED -> {
                    // Ready for retrieval
                }
            }
        }

        if (!missing.isEmpty() || !pending.isEmpty()
                || !processing.isEmpty() || !failed.isEmpty()) {

            throw new IllegalStateException(
                    "Selected papers are not ready. " +
                            "Missing: " + missing.size() +
                            ", Pending: " + pending.size() +
                            ", Processing: " + processing.size() +
                            ", Failed: " + failed.size()
            );
        }
    }
    private void scheduleMissingDocuments(List<UUID> paperIds) {

        List<PaperDocument> documents =
                paperDocumentRepository.findAllByPaper_IdIn(paperIds);

        Set<UUID> existingPaperIds = documents.stream()
                .map(document -> document.getPaper().getId())
                .collect(Collectors.toSet());

        for (UUID paperId : paperIds) {
            if (!existingPaperIds.contains(paperId)) {
                schedulingService.scheduleIfNeeded(paperId);
            }
        }
    }

    private List<SessionRagChunkResponse> mapToResponses(
            List<RetrievedChunk> chunks
    ) {
        return chunks.stream()
                .map(chunk -> new SessionRagChunkResponse(
                        chunk.getId(),
                        chunk.getPaperId(),
                        chunk.getChunkIndex(),
                        chunk.getPageNumber(),
                        chunk.getSectionTitle(),
                        chunk.getContent(),
                        chunk.getDistance()
                ))
                .toList();
    }
}