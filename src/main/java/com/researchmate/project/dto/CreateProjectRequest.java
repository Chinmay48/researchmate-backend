package com.researchmate.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 300,message = "Title cannot exceed 300 characters")
        String title,
        @Size(max=5000,message = "Description cannot exceed 5000 characters")
        String description,
        @Size(max=5000,message = "Description cannot exceed 5000 characters")
        String researchQuestion
) {
}
