package com.researchmate.ai.controller;

import com.researchmate.ai.dto.SessionRagAnswerRequest;
import com.researchmate.ai.dto.SessionRagAnswerResponse;
import com.researchmate.ai.service.SessionRagAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions/{sessionId}/rag")
@RequiredArgsConstructor
public class SessionRagAnswerController {

    private final SessionRagAnswerService answerService;

    @PostMapping("/answer")
    public ResponseEntity<SessionRagAnswerResponse> generateAnswer(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SessionRagAnswerRequest request,
            Authentication authentication) {

        SessionRagAnswerResponse response =
                answerService.generateAnswer(
                        sessionId,
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}