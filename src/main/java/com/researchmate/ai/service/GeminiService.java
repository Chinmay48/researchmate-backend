package com.researchmate.ai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.ThinkingConfig;
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client geminiClient;

    @Value("${gemini.api.model}")
    private String model;

    public String generateResponse(String prompt) {

        long startTime = System.currentTimeMillis();

        System.out.println("Sending request to Gemini...");

        GenerateContentConfig config = GenerateContentConfig.builder()
                .thinkingConfig(
                        ThinkingConfig.builder()
                                .thinkingBudget(0)
                                .build()
                )
                .build();

        GenerateContentResponse response =
                geminiClient.models.generateContent(
                        model,
                        prompt,
                        config
                );

        long endTime = System.currentTimeMillis();

        System.out.println(
                "Gemini API execution time: "
                        + (endTime - startTime) + " ms"
        );

        return response.text();
    }
}