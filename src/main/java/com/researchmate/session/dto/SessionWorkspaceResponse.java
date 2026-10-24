package com.researchmate.session.dto;

import com.researchmate.session.entity.SearchType;
import com.researchmate.session.entity.SessionStatus;
import com.researchmate.sessionpaper.dto.SessionPaperResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SessionWorkspaceResponse(
        UUID sessionId,
        UUID projectId,
        String title,
        String query,
        SearchType searchType,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int totalPapers,
        List<SessionPaperResponse> papers
) {}