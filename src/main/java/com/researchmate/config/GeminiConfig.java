package com.researchmate.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public Client geminiClient() {

        String apiKey = System.getenv("GEMINI_KEY");

        System.out.println("========== KEY DIAGNOSTICS ==========");
        System.out.println("Is key null: " + (apiKey == null));
        System.out.println("Key length: "
                + (apiKey == null ? 0 : apiKey.length()));
        System.out.println("=====================================");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_KEY is missing");
        }

        return Client.builder()
                .apiKey(apiKey.trim())
                .build();
    }
}