package com.researchmate.session.service;


import com.researchmate.exception.ResourceNotFoundException;
import com.researchmate.session.dto.SessionWorkspaceResponse;
import com.researchmate.session.entity.ResearchSession;
import com.researchmate.session.repository.ResearchSessionRepository;
import com.researchmate.sessionpaper.dto.SessionPaperResponse;
import com.researchmate.sessionpaper.entity.SessionPaper;
import com.researchmate.sessionpaper.repository.SessionPaperRepository;
import com.researchmate.user.entity.User;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionWorkspaceService {
    private final UserRepository userRepository;
    private final ResearchSessionRepository sessionRepository;
    private final SessionPaperRepository sessionPaperRepository;

    public SessionWorkspaceResponse getWorkspace(UUID projectId,UUID sessionId,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        ResearchSession session=sessionRepository.findByIdAndProjectIdAndProjectUserId(sessionId,projectId,user.getId()).orElseThrow(()->new ResourceNotFoundException("Session not found"));
        List<SessionPaperResponse> papers=sessionPaperRepository.findAllBySession_IdOrderByRankAsc(sessionId).stream().map(this::toPaperResponse).toList();
        return new SessionWorkspaceResponse(session.getId(),session.getProject().getId(),session.getTitle(),
                session.getQuery(),
                session.getSearchType(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                papers.size(),
                papers
        );

    }

    private SessionPaperResponse toPaperResponse(SessionPaper sessionPaper) {
        var paper = sessionPaper.getPaper();

        return new SessionPaperResponse(
                paper.getId(),
                paper.getExternalId(),
                paper.getTitle(),
                paper.getAuthors(),
                paper.getAbstractText(),
                paper.getCategories(),
                paper.getPublishedAt(),
                paper.getPaperUrl(),
                paper.getPdfUrl(),
                sessionPaper.getRank(),
                sessionPaper.getKeywordScore(),
                sessionPaper.getSemanticScore(),
                sessionPaper.getHybridScore()
        );
    }
}
