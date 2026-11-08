package com.researchmate.chat.controller;

import com.researchmate.chat.service.ChatPaperSelectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.researchmate.chat.dto.ChatPaperSelectionResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats/{chatId}/papers")
@RequiredArgsConstructor
public class ChatPaperSelectionController {

    private final ChatPaperSelectionService selectionService;

    public record UpdateChatPaperSelectionRequest(
            @NotNull(message = "Paper IDs cannot be null")
            List<@NotNull UUID> paperIds
    ) {}

    @PutMapping("/selection")
    public ResponseEntity<Void> updatePaperSelection(
            @PathVariable UUID chatId,
            @Valid @RequestBody UpdateChatPaperSelectionRequest request,
            Authentication authentication
    ) {

        selectionService.updateSelection(
                chatId,
                authentication.getName(),
                request.paperIds()
        );

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/selection")
    public ResponseEntity<ChatPaperSelectionResponse> getPaperSelection(
            @PathVariable UUID chatId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                selectionService.getSelection(
                        chatId,
                        authentication.getName()
                )
        );
    }
}