package com.researchmate.sessionpaper.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SessionRagQueryRequest(

        @NotBlank(message = "Query cannot be empty")
        @Size(max = 2000, message = "Query cannot exceed 2000 characters")
        String query,

        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 20, message = "topK cannot exceed 20")
        Integer topK

) {
    public SessionRagQueryRequest {
        if (topK == null) {
            topK = 5;
        }
    }
}