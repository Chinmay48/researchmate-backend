package com.researchmate.sessionpaper.repository;

import com.researchmate.sessionpaper.entity.SessionPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionPaperRepository extends JpaRepository<SessionPaper,UUID> {
    List<SessionPaper> findAllBySession_IdOrderByRankAsc(UUID sessionId);
    void deleteAllBySession_Id(UUID sessionId);
    @Modifying
    @Query("""
    UPDATE SessionPaper sp
    SET sp.selected = false
    WHERE sp.session.id = :sessionId
""")
    int deselectAllBySessionId(@Param("sessionId") UUID sessionId);

    @Modifying
    @Query("""
    UPDATE SessionPaper sp
    SET sp.selected = true
    WHERE sp.session.id = :sessionId
      AND sp.paper.id IN :paperIds
""")
    int selectPapers(
            @Param("sessionId") UUID sessionId,
            @Param("paperIds") List<UUID> paperIds
    );

    List<SessionPaper> findAllBySession_IdAndSelectedTrueOrderByRankAsc(
            UUID sessionId
    );
}
