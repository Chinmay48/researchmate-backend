package com.researchmate.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSessionRequest (
        @NotBlank(message = "Session title is required")
        @Size(max = 300,message = "Session title cannot exceed 300 charcaterss")
        String title,

        @NotBlank(message = "Research query is required")
        @Size(max = 5000,message = "Research query cannot exceed 5000 characters")
        String query

){

}