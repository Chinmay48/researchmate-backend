package com.researchmate.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatResponse(

        UUID id,
        UUID sessionId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}