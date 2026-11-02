package com.researchmate.chat.controller;

import com.researchmate.chat.dto.ChatResponse;
import com.researchmate.chat.dto.CreateChatRequest;
import com.researchmate.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/{sessionId}/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> createChat(
            @PathVariable UUID sessionId,
            @Valid @RequestBody(required = false) CreateChatRequest request,
            Authentication authentication) {

        String title = request == null ? null : request.title();

        ChatResponse response = chatService.createChat(
                sessionId,
                authentication.getName(),
                title
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getSessionChats(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        List<ChatResponse> chats = chatService.getSessionChats(
                sessionId,
                authentication.getName()
        );

        return ResponseEntity.ok(chats);
    }
}