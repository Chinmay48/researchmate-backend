package com.researchmate.project.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String title,
        String description,
        String researchQuestion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
