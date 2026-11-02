package com.researchmate.chat.dto;

import com.researchmate.ai.enums.AnswerMode;
import com.researchmate.chat.entity.MessageRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatMessageResponse(

        UUID id,
        MessageRole role,
        String content,
        AnswerMode answerMode,
        LocalDateTime createdAt,
        List<MessageSourceResponse> sources

) {}