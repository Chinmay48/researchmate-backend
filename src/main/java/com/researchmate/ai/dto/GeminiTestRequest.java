package com.researchmate.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeminiTestRequest(

        @NotBlank(message = "Prompt cannot be empty")
        @Size(max = 5000, message = "Prompt cannot exceed 5000 characters")
        String prompt

) {}