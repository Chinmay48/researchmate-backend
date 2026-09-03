package com.researchmate.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 300)
        String title,
        @Size(max = 5000,message = "Description is required")
        String description,
        @Size(max = 5000,message = "Research Question is required")
        String researchQuestion
) {
}
