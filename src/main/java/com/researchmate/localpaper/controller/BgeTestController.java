package com.researchmate.localpaper.controller;

import com.researchmate.localpaper.embedding.BgeEmbeddingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BgeTestController {

    private final BgeEmbeddingService embeddingService;

    public BgeTestController(BgeEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @GetMapping("/api/local-papers/test-embedding")
    public Map<String, Object> testEmbedding(
            @RequestParam String query
    ) {

        float[] embedding =
                embeddingService.generateEmbedding(query);

        return Map.of(
                "query", query,
                "dimension", embedding.length,
                "firstFiveValues", new float[]{
                        embedding[0],
                        embedding[1],
                        embedding[2],
                        embedding[3],
                        embedding[4]
                }
        );
    }
}