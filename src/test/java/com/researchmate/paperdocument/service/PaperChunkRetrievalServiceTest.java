package com.researchmate.paperdocument.service;

import com.researchmate.paperdocument.projection.RetrievedChunk;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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
}