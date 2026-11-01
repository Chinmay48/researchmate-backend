package com.researchmate.sessionpaper.service;


import com.researchmate.session.repository.ResearchSessionRepository;
import com.researchmate.sessionpaper.dto.UpdatePaperSelectionRequest;
import com.researchmate.sessionpaper.entity.SessionPaper;
import com.researchmate.sessionpaper.repository.SessionPaperRepository;
import com.researchmate.user.entity.User;
import com.researchmate.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionPaperSelectionService {

    private final ResearchSessionRepository sessionRepository;
    private final SessionPaperRepository sessionPaperRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateSelection(
            UUID sessionId,
            String email,
            UpdatePaperSelectionRequest request
    ) {
        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();

        // Verify session ownership
        sessionRepository.findByIdAndProject_User_Id(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Research session not found or access denied"
                ));

        // Get all papers discovered in this session
        List<SessionPaper> sessionPapers =
                sessionPaperRepository.findAllBySession_IdOrderByRankAsc(sessionId);

        Set<UUID> availablePaperIds = new HashSet<>();

        for (SessionPaper paper : sessionPapers) {
            availablePaperIds.add(paper.getPaper().getId());
        }

        // Validate submitted paper IDs
        if (!availablePaperIds.containsAll(request.paperIds())) {
            throw new IllegalArgumentException(
                    "One or more papers do not belong to this session"
            );
        }

        // Reset previous selection
        sessionPaperRepository.deselectAllBySessionId(sessionId);

        // Apply new selection
        if (!request.paperIds().isEmpty()) {
            sessionPaperRepository.selectPapers(
                    sessionId,
                    request.paperIds()
            );
        }
    }
}