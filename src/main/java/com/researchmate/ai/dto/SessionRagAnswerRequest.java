package com.researchmate.ai.dto;

import com.researchmate.ai.enums.AnswerMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SessionRagAnswerRequest(

        @NotBlank(message = "Question cannot be empty")
        @Size(max = 2000, message = "Question cannot exceed 2000 characters")
        String question,

        @NotNull(message = "Answer mode is required")
        AnswerMode answerMode,

        @Min(value = 1, message = "topK must be at least 1")
        @Max(value = 20, message = "topK cannot exceed 20")
        Integer topK

) {

    public SessionRagAnswerRequest {
        if (topK == null) {
            topK = 5;
        }
    }
}