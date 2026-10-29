package com.researchmate.paperdocument.repository;

import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.entity.PaperProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
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

    @Modifying
    @Query(value = """
        UPDATE paper_documents
        SET status = 'PROCESSING',
            attempt_count = attempt_count + 1,
            started_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP,
            last_error = NULL
        WHERE id = :documentId
          AND status = 'PENDING'
        """, nativeQuery = true)
    int claimPendingDocument(@Param("documentId") UUID documentId);

    List<PaperDocument> findTop10ByStatusOrderByCreatedAtAsc(
            PaperProcessingStatus status
    );
    @Modifying
    @Query(value = """
        UPDATE paper_documents
        SET status = 'COMPLETED',
            completed_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP,
            last_error = NULL
        WHERE id = :documentId
          AND status = 'PROCESSING'
        """, nativeQuery = true)
    int markCompleted(@Param("documentId") UUID documentId);

    @Modifying
    @Query(value = """
        UPDATE paper_documents
        SET status = CASE
                WHEN attempt_count < :maxAttempts THEN 'PENDING'
                ELSE 'FAILED'
            END,
            last_error = :error,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :documentId
          AND status = 'PROCESSING'
        """, nativeQuery = true)
    int recordFailure(
            @Param("documentId") UUID documentId,
            @Param("maxAttempts") int maxAttempts,
            @Param("error") String error
    );
}