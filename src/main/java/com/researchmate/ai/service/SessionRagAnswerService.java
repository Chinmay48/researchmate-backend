package com.researchmate.ai.service;

import com.researchmate.ai.dto.SessionRagAnswerRequest;
import com.researchmate.ai.dto.SessionRagAnswerResponse;
import com.researchmate.sessionpaper.dto.SessionRagChunkResponse;
import com.researchmate.sessionpaper.service.SessionPaperRagRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionRagAnswerService {

    private final SessionPaperRagRetrievalService retrievalService;
    private final RagPromptBuilder promptBuilder;
    private final GeminiService geminiService;

    public SessionRagAnswerResponse generateAnswer(
            java.util.UUID sessionId,
            String email,
            SessionRagAnswerRequest request) {

        // 1. Retrieve relevant chunks from selected papers
        List<SessionRagChunkResponse> chunks =
                retrievalService.retrieveSessionChunks(
                        sessionId,
                        email,
                        request.question(),
                        request.topK()
                );

        // 2. Construct prompt according to answer mode
        String prompt = promptBuilder.buildPrompt(
                request.question(),
                request.answerMode(),
                chunks
        );

        // 3. Generate answer using Gemini
        String answer = geminiService.generateResponse(prompt);

        // 4. Prepare source references
        List<SessionRagAnswerResponse.SourceCitation> sources =
                chunks.stream()
                        .map(chunk ->
                                new SessionRagAnswerResponse.SourceCitation(
                                        chunk.paperId(),
                                        chunk.chunkId(),
                                        chunk.pageNumber(),
                                        chunk.sectionTitle(),
                                        chunk.distance()
                                )
                        )
                        .toList();

        // 5. Return final response
        return new SessionRagAnswerResponse(
                answer,
                request.answerMode(),
                sources
        );
    }
}