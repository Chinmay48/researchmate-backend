package com.researchmate.ai.controller;

import com.researchmate.ai.dto.GeminiTestRequest;
import com.researchmate.ai.service.GeminiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class GeminiTestController {

    private final GeminiService geminiService;

    @PostMapping("/test")
    public ResponseEntity<String> testGemini(
            @Valid @RequestBody GeminiTestRequest request) {

        String response = geminiService.generateResponse(request.prompt());

        return ResponseEntity.ok(response);
    }
}