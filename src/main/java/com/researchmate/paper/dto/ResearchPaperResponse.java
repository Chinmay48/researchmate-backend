package com.researchmate.paper.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResearchPaperResponse(
        UUID id,
        UUID sessionId,
        String arxivId,
        String title,
        String authors,
        String abstractText,
        LocalDateTime publishedAt,
        LocalDateTime updatedAt,
        String categories,
        String paperUrl,
        String pdfUrl,
        LocalDateTime createdAt
) {
}