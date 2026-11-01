package com.researchmate.sessionpaper.controller;

import com.researchmate.sessionpaper.dto.UpdatePaperSelectionRequest;
import com.researchmate.sessionpaper.service.SessionPaperSelectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/{sessionId}/papers")
@RequiredArgsConstructor
public class SessionPaperSelectionController {

    private final SessionPaperSelectionService selectionService;

    @PutMapping("/selection")
    public ResponseEntity<Void> updatePaperSelection(
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdatePaperSelectionRequest request,
            Authentication authentication
    ) {

        selectionService.updateSelection(
                sessionId,
                authentication.getName(),
                request
        );

        return ResponseEntity.noContent().build();
    }
}