package com.researchmate.localpaper.dto;

import com.researchmate.localpaper.entity.LocalPaper;

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
        double similarityScore
) {

    public static LocalPaperSemanticSearchResult from(
            LocalPaper paper,
            double similarityScore
    ) {
        return new LocalPaperSemanticSearchResult(
                paper.getId(),
                paper.getExternalId(),
                paper.getTitle(),
                paper.getAuthors(),
                paper.getAbstractText(),
                paper.getCategories(),
                paper.getPublishedAt(),
                paper.getUpdatedAt(),
                paper.getPaperUrl(),
                paper.getPdfUrl(),
                similarityScore
        );
    }
}