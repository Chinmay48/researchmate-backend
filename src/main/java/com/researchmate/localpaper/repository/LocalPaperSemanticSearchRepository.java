package com.researchmate.localpaper.repository;

import com.researchmate.localpaper.entity.LocalPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LocalPaperSemanticSearchRepository
        extends JpaRepository<LocalPaper, UUID> {

    @Query(value = """
    SELECT
        p.id AS id,
        p.external_id AS "externalId",
        p.title AS title,
        p.authors AS authors,
        p.abstract_text AS "abstractText",
        p.categories AS categories,
        p.published_at AS "publishedAt",
        p.updated_at AS "updatedAt",
        p.paper_url AS "paperUrl",
        p.pdf_url AS "pdfUrl",

        1.0 - (
            e.embedding <=> CAST(:queryEmbedding AS vector)
        ) AS "similarityScore"

    FROM local_papers p
    JOIN paper_embeddings e
        ON p.id = e.paper_id

    ORDER BY e.embedding <=> CAST(:queryEmbedding AS vector)
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM local_papers p
    JOIN paper_embeddings e
        ON p.id = e.paper_id
    """,
            nativeQuery = true)
    Page<LocalPaperSemanticSearchProjection> semanticSearch(
            @Param("queryEmbedding") String queryEmbedding,
            Pageable pageable
    );
}