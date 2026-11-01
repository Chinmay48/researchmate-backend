package com.researchmate.sessionpaper.controller;

import com.researchmate.sessionpaper.dto.SessionRagChunkResponse;
import com.researchmate.sessionpaper.dto.SessionRagQueryRequest;
import com.researchmate.sessionpaper.service.SessionPaperRagRetrievalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/{sessionId}/rag")
@RequiredArgsConstructor
public class SessionPaperRagController {

    private final SessionPaperRagRetrievalService retrievalService;

    @PostMapping("/retrieve")
    public ResponseEntity<List<SessionRagChunkResponse>> retrieveChunks(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SessionRagQueryRequest request,
            Authentication authentication
    ) {

        List<SessionRagChunkResponse> response =
                retrievalService.retrieveSessionChunks(
                        sessionId,
                        authentication.getName(),
                        request.query(),
                        request.topK()
                );

        return ResponseEntity.ok(response);
    }
}