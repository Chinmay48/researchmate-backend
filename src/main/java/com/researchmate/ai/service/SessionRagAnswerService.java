package com.researchmate.ai.service;

import com.researchmate.ai.dto.SessionRagAnswerRequest;
import com.researchmate.ai.dto.SessionRagAnswerResponse;
import com.researchmate.chat.service.ChatMessagePersistenceService;
import com.researchmate.chat.service.ChatService;
import com.researchmate.sessionpaper.dto.SessionRagChunkResponse;
import com.researchmate.sessionpaper.service.SessionPaperRagRetrievalService;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionRagAnswerService {

    private final SessionPaperRagRetrievalService retrievalService;
    private final RagPromptBuilder promptBuilder;
    private final GeminiService geminiService;

    private final ChatService chatService;
    private final ChatMessagePersistenceService persistenceService;
    private final UserRepository userRepository;

    public SessionRagAnswerResponse generateAnswer(
            UUID sessionId,
            String email,
            SessionRagAnswerRequest request) {

        // 1. Validate chat ownership and session relationship
        chatService.validateChatAccess(
                request.chatId(),
                sessionId,
                email
        );

        // 2. Retrieve relevant chunks from selected papers
        List<SessionRagChunkResponse> chunks =
                retrievalService.retrieveChatChunks(
                        sessionId,
                        request.chatId(),
                        email,
                        request.question(),
                        request.topK()
                );

        // 3. Save the user's question
        persistenceService.saveUserMessage(
                request.chatId(),
                request.question()
        );

        // 4. Construct the prompt
        String prompt = promptBuilder.buildPrompt(
                request.question(),
                request.answerMode(),
                chunks
        );

        // 5. Generate the answer using Gemini
        String answer = geminiService.generateResponse(prompt);

        // 6. Save Gemini's answer and its exact retrieved sources
        persistenceService.saveAssistantMessage(
                request.chatId(),
                answer,
                request.answerMode(),
                chunks
        );

        // 7. Prepare source references for API response
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

        return new SessionRagAnswerResponse(
                answer,
                request.answerMode(),
                sources
        );
    }
}