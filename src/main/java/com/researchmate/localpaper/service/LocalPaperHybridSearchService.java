package com.researchmate.localpaper.service;

import com.researchmate.localpaper.dto.LocalPaperSemanticSearchResult;
import com.researchmate.localpaper.embedding.BgeEmbeddingService;
import com.researchmate.localpaper.repository.LocalPaperHybridSearchRepository;
import com.researchmate.localpaper.repository.LocalPaperSemanticSearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class LocalPaperHybridSearchService {

    private final BgeEmbeddingService embeddingService;
    private final LocalPaperHybridSearchRepository repository;

    public LocalPaperHybridSearchService(
            BgeEmbeddingService embeddingService,
            LocalPaperHybridSearchRepository repository
    ) {
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    public Page<LocalPaperSemanticSearchResult> search(
            String query,
            int page,
            int size
    ) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }

        float[] embedding = embeddingService.generateEmbedding(query);

        String vector = toPgVector(embedding);

        PageRequest pageable = PageRequest.of(page, size);

        Page<LocalPaperSemanticSearchProjection> results =
                repository.hybridSearch(
                        query,
                        vector,
                        pageable
                );

        return results.map(result ->
                new LocalPaperSemanticSearchResult(
                        result.getId(),
                        result.getExternalId(),
                        result.getTitle(),
                        result.getAuthors(),
                        result.getAbstractText(),
                        result.getCategories(),
                        result.getPublishedAt(),
                        result.getUpdatedAt(),
                        result.getPaperUrl(),
                        result.getPdfUrl(),
                        result.getKeywordScore(),
                        result.getSemanticScore(),
                        result.getHybridScore()
                )
        );
    }

    private String toPgVector(float[] embedding) {
        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(embedding[i]);
        }

        builder.append("]");

        return builder.toString();
    }
}