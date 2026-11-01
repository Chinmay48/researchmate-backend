package com.researchmate.paperdocument.service;

import com.researchmate.paperdocument.projection.RetrievedChunk;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class PaperChunkRetrievalServiceTest {

    @Autowired
    private PaperChunkRetrievalService retrievalService;

    @Test
    void retrieveRelevantChunksTest() {

        String query = "How does transfer learning help estimate brain strain and strain rate?";

        List<RetrievedChunk> chunks =
                retrievalService.retrieveRelevantChunks(query, 5);

        System.out.println("\n========== RETRIEVED CHUNKS ==========");

        for (RetrievedChunk chunk : chunks) {
            System.out.println("Paper ID: " + chunk.getPaperId());
            System.out.println("Page: " + chunk.getPageNumber());
            System.out.println("Chunk Index: " + chunk.getChunkIndex());
            System.out.println("Distance: " + chunk.getDistance());
            System.out.println("Content: " + chunk.getContent());
            System.out.println("--------------------------------------");
        }

        System.out.println("Total retrieved: " + chunks.size());
    }
    @Test
    void retrieveChunksFromSelectedPapersTest() {

        UUID selectedPaperId = UUID.fromString(
                "7a30a472-7bc9-4863-b551-b77b000d3e63"
        );

        List<UUID> selectedPaperIds = List.of(selectedPaperId);

        String query = "How does transfer learning help estimate brain strain?";

        List<RetrievedChunk> chunks =
                retrievalService.retrieveRelevantChunks(
                        query,
                        selectedPaperIds,
                        5
                );

        System.out.println("\n========== SELECTED PAPER RETRIEVAL ==========");

        for (RetrievedChunk chunk : chunks) {

            System.out.println("Paper ID: " + chunk.getPaperId());
            System.out.println("Page: " + chunk.getPageNumber());
            System.out.println("Distance: " + chunk.getDistance());
            System.out.println("Content: " + chunk.getContent());

            // Verify that no unselected paper is returned
            assertEquals(selectedPaperId, chunk.getPaperId());
        }

        assertFalse(chunks.isEmpty());

        System.out.println("Total retrieved: " + chunks.size());
    }
}