package com.researchmate.paperdocument.service;

import com.researchmate.localpaper.embedding.BgeEmbeddingService;
import com.researchmate.paperdocument.projection.RetrievedChunk;
import com.researchmate.paperdocument.repository.PaperChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaperChunkRetrievalService {

    private final BgeEmbeddingService embeddingService;
    private final PaperChunkRepository chunkRepository;
    public List<RetrievedChunk> retrieveRelevantChunks(
            String query,
            List<UUID> paperIds,
            int topK
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        if (paperIds == null || paperIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one paper must be selected"
            );
        }

        if (topK < 1 || topK > 50) {
            throw new IllegalArgumentException(
                    "topK must be between 1 and 50"
            );
        }

        // Generate query embedding
        float[] embedding = embeddingService.generateEmbedding(query);

        // Convert embedding to pgvector format
        String vector = toVectorString(embedding);

        // Search only within the selected papers
        return chunkRepository.findSimilarChunksInPapers(
                vector,
                paperIds,
                topK
        );
    }
    public List<RetrievedChunk> retrieveRelevantChunks(
            String query,
            int topK
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        if (topK < 1 || topK > 50) {
            throw new IllegalArgumentException(
                    "topK must be between 1 and 50"
            );
        }

        // Generate query embedding
        float[] embedding = embeddingService.generateEmbedding(query);

        // Convert float array to pgvector format
        String vector = toVectorString(embedding);

        // Retrieve the most relevant chunks
        return chunkRepository.findSimilarChunks(vector, topK);
    }

    private String toVectorString(float[] embedding) {
        StringBuilder vector = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                vector.append(",");
            }
            vector.append(embedding[i]);
        }

        vector.append("]");
        return vector.toString();
    }
}