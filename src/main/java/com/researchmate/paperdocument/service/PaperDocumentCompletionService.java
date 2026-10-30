package com.researchmate.paperdocument.service;

import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.repository.PaperDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaperDocumentCompletionService {

    private final PaperChunkPersistenceService persistenceService;
    private final PaperDocumentRepository documentRepository;

    @Transactional
    public int completeDocument(
            UUID documentId,
            PaperDocument document,
            List<PaperChunkingService.ChunkData> chunks
    ) {
        int persistedCount = persistenceService.persistChunks(document, chunks);

        int updated = documentRepository.markCompleted(documentId);

        if (updated != 1) {
            throw new IllegalStateException(
                    "Could not mark document as completed"
            );
        }

        return persistedCount;
    }
}