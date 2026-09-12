package com.researchmate.session.dto;

public record ResearchSearchResponse(
        String sessionId,
        String query,
        String results
) {
}
