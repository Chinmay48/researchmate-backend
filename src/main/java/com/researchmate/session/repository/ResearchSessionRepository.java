package com.researchmate.session.repository;

import com.researchmate.session.entity.ResearchSession;
import com.researchmate.session.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResearchSessionRepository extends JpaRepository<ResearchSession, UUID> {
    List<ResearchSession> findAllByProjectIdAndProjectUserId(UUID projectId,UUID userId);
    Optional<ResearchSession> findByIdAndProjectIdAndProjectUserId(UUID sessionId,UUID projectId,UUID userId);
    List<ResearchSession> findAllByProjectIdAndStatus(UUID projectId, SessionStatus status);
    Optional<ResearchSession> findByIdAndProjectId(
            UUID sessionId,
            UUID projectId
    );
}
