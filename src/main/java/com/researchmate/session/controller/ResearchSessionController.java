package com.researchmate.session.controller;


import com.researchmate.session.dto.CreateSessionRequest;
import com.researchmate.session.dto.SessionResponse;
import com.researchmate.session.service.ResearchSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/sessions")
@RequiredArgsConstructor
public class ResearchSessionController {
    private final ResearchSessionService researchSessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@PathVariable UUID projectId, @Valid @RequestBody CreateSessionRequest request, Authentication authentication){
        SessionResponse response=researchSessionService.createSession(projectId,request,authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getProjectSessions(@PathVariable UUID projectId,Authentication authentication){
        List<SessionResponse> sessions=researchSessionService.getProjectSession(projectId,authentication.getName());
        return ResponseEntity.ok(sessions);
    }
}
