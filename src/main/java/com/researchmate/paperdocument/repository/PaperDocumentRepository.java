package com.researchmate.paperdocument.repository;

import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.entity.PaperProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface PaperDocumentRepository
        extends JpaRepository<PaperDocument, UUID> {

    Optional<PaperDocument> findByPaper_Id(UUID paperId);

    boolean existsByPaper_Id(UUID paperId);

    Optional<PaperDocument> findByPaper_IdAndStatus(
            UUID paperId,
            PaperProcessingStatus status
    );
    @Modifying
    @Query(value = """
        INSERT INTO paper_documents (
            id, paper_id, status, attempt_count,
            pipeline_version, created_at, updated_at
        )
        VALUES (
            :id, :paperId, 'PENDING', 0,
            'v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        )
        ON CONFLICT (paper_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("paperId") UUID paperId
    );
}