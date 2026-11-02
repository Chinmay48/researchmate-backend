package com.researchmate.chat.dto;

import jakarta.validation.constraints.Size;

public record CreateChatRequest(

        @Size(max = 200, message = "Chat title cannot exceed 200 characters")
        String title

) {}