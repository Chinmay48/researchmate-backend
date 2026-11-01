package com.researchmate.sessionpaper.service;

import com.researchmate.sessionpaper.entity.SessionPaper;
import com.researchmate.sessionpaper.repository.SessionPaperRepository;
import com.researchmate.session.repository.ResearchSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionPaperValidationService {

    private final ResearchSessionRepository sessionRepository;
    private final SessionPaperRepository sessionPaperRepository;

    public List<UUID> getSelectedPaperIds(UUID sessionId, UUID userId) {

        // Verify session ownership
        sessionRepository.findByIdAndProject_User_Id(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Research session not found or access denied"
                ));

        // Fetch only selected papers
        List<SessionPaper> sessionPapers =
                sessionPaperRepository
                        .findAllBySession_IdAndSelectedTrueOrderByRankAsc(sessionId);

        if (sessionPapers.isEmpty()) {
            throw new IllegalStateException(
                    "No papers have been selected for this session"
            );
        }

        return sessionPapers.stream()
                .map(sessionPaper -> sessionPaper.getPaper().getId())
                .toList();
    }
}