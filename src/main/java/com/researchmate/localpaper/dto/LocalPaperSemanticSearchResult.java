package com.researchmate.localpaper.dto;

import java.time.Instant;
import java.util.UUID;

public record LocalPaperSemanticSearchResult(
        UUID id,
        String externalId,
        String title,
        String authors,
        String abstractText,
        String categories,
        Instant publishedAt,
        Instant updatedAt,
        String paperUrl,
        String pdfUrl,
        Double keywordScore,
        Double semanticScore,
        Double hybridScore
) {
}