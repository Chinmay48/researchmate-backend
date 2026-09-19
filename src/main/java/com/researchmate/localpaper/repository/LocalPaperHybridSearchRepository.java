package com.researchmate.localpaper.repository;

import com.researchmate.localpaper.entity.LocalPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LocalPaperHybridSearchRepository
        extends JpaRepository<LocalPaper, UUID> {

    @Query(value = """
        WITH semantic_candidates AS (
            SELECT
                paper_id,
                semantic_score,
                ROW_NUMBER() OVER (
                    ORDER BY semantic_score DESC, paper_id
                ) AS semantic_rank
            FROM (
                SELECT
                    e.paper_id,
                    1.0 - (
                        e.embedding <=> CAST(:queryEmbedding AS vector)
                    ) AS semantic_score
                FROM paper_embeddings e
                ORDER BY e.embedding <=> CAST(:queryEmbedding AS vector)
                LIMIT 1000
            ) s
        ),

        keyword_candidates AS (
            SELECT
                paper_id,
                keyword_score,
                ROW_NUMBER() OVER (
                    ORDER BY keyword_score DESC, paper_id
                ) AS keyword_rank
            FROM (
                SELECT
                    p.id AS paper_id,
                    ts_rank(
                        p.search_vector,
                        websearch_to_tsquery('english', :query),
                        32
                    ) AS keyword_score
                FROM local_papers p
                WHERE p.search_vector @@
                      websearch_to_tsquery('english', :query)
                ORDER BY keyword_score DESC, p.id
                LIMIT 1000
            ) k
        ),

        candidates AS (
            SELECT paper_id FROM semantic_candidates
            UNION
            SELECT paper_id FROM keyword_candidates
        )

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

            COALESCE(k.keyword_score, 0)
                AS "keywordScore",

            COALESCE(s.semantic_score, 0)
                AS "semanticScore",

            (
                0.4 * COALESCE(
                    60.0 / (60.0 + k.keyword_rank),
                    0
                )
                +
                0.6 * COALESCE(
                    60.0 / (60.0 + s.semantic_rank),
                    0
                )
            ) AS "hybridScore"

        FROM candidates c

        JOIN local_papers p
            ON p.id = c.paper_id

        LEFT JOIN semantic_candidates s
            ON s.paper_id = p.id

        LEFT JOIN keyword_candidates k
            ON k.paper_id = p.id

        ORDER BY "hybridScore" DESC
        """,

            countQuery = """
        WITH semantic_candidates AS (
            SELECT e.paper_id
            FROM paper_embeddings e
            ORDER BY e.embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT 1000
        ),

        keyword_candidates AS (
            SELECT p.id AS paper_id
            FROM local_papers p
            WHERE p.search_vector @@
                  websearch_to_tsquery('english', :query)
            ORDER BY ts_rank(
                p.search_vector,
                websearch_to_tsquery('english', :query),
                32
            ) DESC
            LIMIT 1000
        )

        SELECT COUNT(*)
        FROM (
            SELECT paper_id FROM semantic_candidates
            UNION
            SELECT paper_id FROM keyword_candidates
        ) candidates
        """,

            nativeQuery = true)
    Page<LocalPaperSemanticSearchProjection> hybridSearch(
            @Param("query") String query,
            @Param("queryEmbedding") String queryEmbedding,
            Pageable pageable
    );
}