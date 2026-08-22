package com.researchmate.auth.dto;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String name,
        String email,
        String token
) {
}