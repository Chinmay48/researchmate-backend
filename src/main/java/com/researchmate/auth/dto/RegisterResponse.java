package com.researchmate.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String name,
        String email,
        String message
) {
}
