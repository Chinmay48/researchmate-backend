package com.researchmate.paperdocument.service;

import com.researchmate.localpaper.embedding.BgeEmbeddingService;
import com.researchmate.paperdocument.entity.PaperChunk;
import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.repository.PaperChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class PaperChunkPersistenceService {

    private final BgeEmbeddingService embeddingService;
    private final PaperChunkRepository paperChunkRepository;

    @Transactional
    public int persistChunks(
            PaperDocument document,
            List<PaperChunkingService.ChunkData> chunks
    ) {
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot persist a document without chunks"
            );
        }

        paperChunkRepository.deleteAllByPaperDocument_Id(document.getId());

        for (PaperChunkingService.ChunkData chunk : chunks) {
            PaperChunk savedChunk = paperChunkRepository.save(
                    PaperChunk.builder()
                            .paperDocument(document)
                            .chunkIndex(chunk.chunkIndex())
                            .pageNumber(chunk.pageNumber())
                            .content(chunk.content())
                            .build()
            );

            float[] vector = embeddingService.generateEmbedding(
                    chunk.content()
            );

            if (vector.length != 384) {
                throw new IllegalStateException(
                        "Expected 384-dimensional embedding"
                );
            }

            String vectorLiteral = Arrays.stream(toDoubleArray(vector))
                    .mapToObj(Double::toString)
                    .collect(Collectors.joining(",", "[", "]"));

            int updated = paperChunkRepository.updateEmbedding(
                    savedChunk.getId(),
                    vectorLiteral
            );

            if (updated != 1) {
                throw new IllegalStateException(
                        "Failed to persist chunk embedding: "
                                + savedChunk.getId()
                );
            }
        }

        long persistedCount =
                paperChunkRepository.countByPaperDocument_Id(document.getId());

        if (persistedCount != chunks.size()) {
            throw new IllegalStateException(
                    "Persisted chunk count does not match expected count"
            );
        }

        return chunks.size();
    }

    private double[] toDoubleArray(float[] vector) {
        double[] result = new double[vector.length];

        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i];
        }

        return result;
    }
}