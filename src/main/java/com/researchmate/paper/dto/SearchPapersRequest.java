package com.researchmate.paper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchPapersRequest(
        @NotBlank(message = "Search query cannot be blank")
        @Size(min = 2,max = 500,message = "Search query must contain between 2 and 500 characters")
        String query
) {
}
