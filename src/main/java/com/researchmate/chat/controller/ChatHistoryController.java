package com.researchmate.chat.controller;

import com.researchmate.chat.dto.ChatMessageResponse;
import com.researchmate.chat.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @PathVariable UUID chatId,
            Authentication authentication) {

        List<ChatMessageResponse> messages =
                chatHistoryService.getChatHistory(
                        chatId,
                        authentication.getName()
                );

        return ResponseEntity.ok(messages);
    }
}