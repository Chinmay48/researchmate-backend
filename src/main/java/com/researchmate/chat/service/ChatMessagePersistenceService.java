package com.researchmate.chat.service;

import com.researchmate.ai.enums.AnswerMode;
import com.researchmate.chat.entity.Chat;
import com.researchmate.chat.entity.ChatMessage;
import com.researchmate.chat.entity.MessageRole;
import com.researchmate.chat.entity.MessageSource;
import com.researchmate.chat.repository.ChatMessageRepository;
import com.researchmate.chat.repository.ChatRepository;
import com.researchmate.chat.repository.MessageSourceRepository;
import com.researchmate.paperdocument.entity.PaperChunk;
import com.researchmate.paperdocument.repository.PaperChunkRepository;
import com.researchmate.sessionpaper.dto.SessionRagChunkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessagePersistenceService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final MessageSourceRepository sourceRepository;
    private final PaperChunkRepository chunkRepository;

    @Transactional
    public UUID saveUserMessage(UUID chatId, String content) {

        Chat chat = getChat(chatId);

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .role(MessageRole.USER)
                .content(content)
                .build();

        ChatMessage savedMessage = messageRepository.save(message);

        chatRepository.save(chat);

        return savedMessage.getId();
    }

    @Transactional
    public UUID saveAssistantMessage(
            UUID chatId,
            String content,
            AnswerMode answerMode,
            List<SessionRagChunkResponse> chunks) {

        Chat chat = getChat(chatId);

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .answerMode(answerMode)
                .build();

        ChatMessage savedMessage = messageRepository.save(message);

        List<MessageSource> sources = chunks.stream()
                .map(chunk -> {

                    PaperChunk paperChunk = chunkRepository.findById(
                            chunk.chunkId()
                    ).orElseThrow(() ->
                            new IllegalStateException(
                                    "Retrieved paper chunk not found: "
                                            + chunk.chunkId()
                            ));

                    return MessageSource.builder()
                            .message(savedMessage)
                            .chunk(paperChunk)
                            .retrievalDistance(chunk.distance())
                            .build();
                })
                .toList();

        sourceRepository.saveAll(sources);

        chatRepository.save(chat);

        return savedMessage.getId();
    }

    private Chat getChat(UUID chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Chat not found"));
    }
}