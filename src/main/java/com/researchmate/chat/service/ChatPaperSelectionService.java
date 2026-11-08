package com.researchmate.chat.service;

import com.researchmate.chat.entity.Chat;
import com.researchmate.chat.entity.ChatPaper;
import com.researchmate.chat.repository.ChatPaperRepository;
import com.researchmate.chat.repository.ChatRepository;
import com.researchmate.sessionpaper.entity.SessionPaper;
import com.researchmate.sessionpaper.repository.SessionPaperRepository;
import com.researchmate.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.researchmate.chat.dto.ChatPaperSelectionResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatPaperSelectionService {

    private final ChatRepository chatRepository;
    private final ChatPaperRepository chatPaperRepository;
    private final SessionPaperRepository sessionPaperRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateSelection(
            UUID chatId,
            String email,
            List<UUID> paperIds
    ) {

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();

        // Verify chat ownership
        Chat chat = chatRepository
                .findByIdAndSession_Project_User_Id(chatId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Chat not found or access denied"
                        ));

        UUID sessionId = chat.getSession().getId();

        // Get papers available in this session
        List<SessionPaper> sessionPapers =
                sessionPaperRepository
                        .findAllBySession_IdOrderByRankAsc(sessionId);

        Set<UUID> availablePaperIds = new HashSet<>();

        for (SessionPaper sessionPaper : sessionPapers) {
            availablePaperIds.add(
                    sessionPaper.getPaper().getId()
            );
        }

        // Validate submitted paper IDs
        if (!availablePaperIds.containsAll(paperIds)) {
            throw new IllegalArgumentException(
                    "One or more papers do not belong to this session"
            );
        }

        // Replace only this chat's selection
        chatPaperRepository.deleteAllByChat_Id(chatId);

        if (!paperIds.isEmpty()) {

            List<ChatPaper> selectedPapers = paperIds.stream()
                    .map(paperId -> ChatPaper.builder()
                            .chat(chat)
                            .paper(
                                    sessionPapers.stream()
                                            .filter(sp ->
                                                    sp.getPaper().getId()
                                                            .equals(paperId))
                                            .findFirst()
                                            .orElseThrow()
                                            .getPaper()
                            )
                            .build())
                    .toList();

            chatPaperRepository.saveAll(selectedPapers);
        }
    }

    @Transactional
    public ChatPaperSelectionResponse getSelection(
            UUID chatId,
            String email
    ) {
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

        List<ChatPaperSelectionResponse.SelectedPaper> papers =
                chatPaperRepository
                        .findAllByChat_IdOrderByCreatedAtAsc(chatId)
                        .stream()
                        .map(cp -> new ChatPaperSelectionResponse.SelectedPaper(
                                cp.getPaper().getId(),
                                cp.getPaper().getTitle(),
                                cp.getPaper().getExternalId()
                        ))
                        .toList();

        return new ChatPaperSelectionResponse(chatId, papers);
    }
}