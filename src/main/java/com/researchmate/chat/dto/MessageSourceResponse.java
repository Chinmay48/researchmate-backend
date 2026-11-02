package com.researchmate.chat.dto;

import java.util.UUID;

public record MessageSourceResponse(
        UUID paperId,
        UUID chunkId,
        Integer pageNumber,
        String sectionTitle,
        Double retrievalDistance
) {}