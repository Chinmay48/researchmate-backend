package com.researchmate.chat.service;

import com.researchmate.chat.dto.ChatResponse;
import com.researchmate.chat.entity.Chat;
import com.researchmate.chat.repository.ChatRepository;
import com.researchmate.session.entity.ResearchSession;
import com.researchmate.session.repository.ResearchSessionRepository;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ResearchSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatResponse createChat(UUID sessionId, String email, String title) {

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();

        ResearchSession session = sessionRepository
                .findByIdAndProject_User_Id(sessionId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Research session not found or access denied"
                        ));

        Chat chat = Chat.builder()
                .session(session)
                .title(title == null || title.isBlank()
                        ? "New Chat"
                        : title.trim())
                .build();
        Chat savedChat = chatRepository.save(chat);

        return mapToResponse(savedChat);

    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getSessionChats(UUID sessionId, String email) {

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();

        sessionRepository.findByIdAndProject_User_Id(sessionId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Research session not found or access denied"
                        ));

        return chatRepository
                .findAllBySession_IdOrderByUpdatedAtDesc(sessionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ChatResponse mapToResponse(Chat chat) {

        return new ChatResponse(
                chat.getId(),
                chat.getSession().getId(),
                chat.getTitle(),
                chat.getCreatedAt(),
                chat.getUpdatedAt()
        );
    }
    @Transactional(readOnly = true)
    public void validateChatAccess(
            UUID chatId,
            UUID sessionId,
            String email) {

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();

        Chat chat = chatRepository
                .findByIdAndSession_Project_User_Id(chatId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Chat not found or access denied"
                        ));

        if (!chat.getSession().getId().equals(sessionId)) {
            throw new IllegalArgumentException(
                    "Chat does not belong to the specified research session"
            );
        }
    }
}