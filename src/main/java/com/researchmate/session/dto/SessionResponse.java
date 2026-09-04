package com.researchmate.session.dto;

import com.researchmate.session.entity.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(

        UUID id,

        UUID projectId,

        String title,

        String query,

        SessionStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}