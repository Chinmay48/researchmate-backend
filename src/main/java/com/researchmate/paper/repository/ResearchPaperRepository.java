package com.researchmate.paper.repository;

import com.researchmate.paper.entity.ResearchPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResearchPaperRepository  extends JpaRepository<ResearchPaper, UUID> {

    List<ResearchPaper> findAllBySessionId(UUID sessionId);
    boolean existsBySessionIdAndArxivId(UUID sessionId,String arxivId);
}
