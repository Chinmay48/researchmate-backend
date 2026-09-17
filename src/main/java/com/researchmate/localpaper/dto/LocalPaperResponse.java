package com.researchmate.localpaper.dto;

import com.researchmate.localpaper.entity.LocalPaper;

import java.time.Instant;
import java.util.UUID;

public record LocalPaperResponse(
        UUID id,
        String externalId,
        String source,
        String title,
        String authors,
        String abstractText,
        String categories,
        Instant publishedAt,
        Instant updatedAt,
        String paperUrl,
        String pdfUrl,
        Double relevanceScore,
        Boolean titleMatched,
        Boolean abstractMatched,
        Boolean authorMatched,
        Boolean categoryMatched,
        String highlightedTitle,
        String highlightedAbstract
) {

    public static LocalPaperResponse fromEntity(LocalPaper paper,Double relevanceScore,Boolean titleMatched,
                                                Boolean abstractMatched,
                                                Boolean authorMatched,
                                                Boolean categoryMatched,String highlightedTitle,
                                                String highlightedAbstract) {
        return new LocalPaperResponse(
                paper.getId(),
                paper.getExternalId(),
                paper.getSource(),
                paper.getTitle(),
                paper.getAuthors(),
                paper.getAbstractText(),
                paper.getCategories(),
                paper.getPublishedAt(),
                paper.getUpdatedAt(),
                paper.getPaperUrl(),
                paper.getPdfUrl(),
                relevanceScore,
                titleMatched,
                abstractMatched,
                authorMatched,
                categoryMatched,
                highlightedTitle,highlightedAbstract

        );
    }
}