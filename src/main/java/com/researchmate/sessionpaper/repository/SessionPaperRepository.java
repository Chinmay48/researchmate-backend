package com.researchmate.sessionpaper.repository;

import com.researchmate.sessionpaper.entity.SessionPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionPaperRepository extends JpaRepository<SessionPaper,UUID> {
    List<SessionPaper> findAllBySession_IdOrderByRankAsc(UUID sessionId);
    void deleteAllBySession_Id(UUID sessionId);

}
