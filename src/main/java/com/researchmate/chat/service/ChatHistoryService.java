package com.researchmate.chat.service;

import com.researchmate.chat.dto.ChatMessageResponse;
import com.researchmate.chat.dto.MessageSourceResponse;
import com.researchmate.chat.entity.Chat;
import com.researchmate.chat.entity.ChatMessage;
import com.researchmate.chat.entity.MessageSource;
import com.researchmate.chat.repository.ChatMessageRepository;
import com.researchmate.chat.repository.ChatRepository;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(
            UUID chatId,
            String email) {

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();

        // Validate chat ownership
        Chat chat = chatRepository
                .findByIdAndSession_Project_User_Id(chatId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Chat not found or access denied"
                        ));

        // Retrieve messages in chronological order
        List<ChatMessage> messages =
                messageRepository.findAllByChat_IdOrderByCreatedAtAsc(
                        chat.getId()
                );

        return messages.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {

        List<MessageSourceResponse> sources =
                message.getSources()
                        .stream()
                        .map(this::mapSource)
                        .toList();

        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getAnswerMode(),
                message.getCreatedAt(),
                sources
        );
    }

    private MessageSourceResponse mapSource(MessageSource source) {

        var chunk = source.getChunk();

        return new MessageSourceResponse(
                chunk.getPaperDocument().getPaper().getId(),
                chunk.getId(),
                chunk.getPageNumber(),
                chunk.getSectionTitle(),
                source.getRetrievalDistance()
        );
    }
}