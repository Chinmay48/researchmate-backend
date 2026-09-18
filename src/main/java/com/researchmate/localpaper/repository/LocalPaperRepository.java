package com.researchmate.localpaper.repository;

import com.researchmate.localpaper.entity.LocalPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocalPaperRepository extends JpaRepository<LocalPaper, UUID> {

    Optional<LocalPaper> findBySourceAndExternalId(
            String source,
            String externalId
    );

    boolean existsBySourceAndExternalId(
            String source,
            String externalId
    );

    Page<LocalPaper> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    Page<LocalPaper> findByTitleContainingIgnoreCaseOrAbstractTextContainingIgnoreCaseOrAuthorsContainingIgnoreCaseOrCategoriesContainingIgnoreCase(
            String titleQuery,
            String abstractQuery,
            String authorsQuery,
            String categoriesQuery,
            Pageable pageable
    );

    @Query(
            value = """
                SELECT
                    id,
                    external_id AS externalId,
                    source,
                    title,
                    authors,
                    abstract_text AS abstractText,
                    categories,
                    published_at AS publishedAt,
                    updated_at AS updatedAt,
                    paper_url AS paperUrl,
                    pdf_url AS pdfUrl,

                    (
                        ts_rank(
                            search_vector,
                            websearch_to_tsquery('english', :query),
                            32
                        )
                        +
                        2.0 * ts_rank(
                            to_tsvector(
                                'english',
                                coalesce(title, '')
                            ),
                            websearch_to_tsquery('english', :query),
                            32
                        )
                    ) AS relevanceScore,

                    CASE
                        WHEN to_tsvector(
                            'english',
                            coalesce(title, '')
                        ) @@ websearch_to_tsquery('english', :query)
                        THEN true
                        ELSE false
                    END AS titleMatched,

                    CASE
                        WHEN to_tsvector(
                            'english',
                            coalesce(abstract_text, '')
                        ) @@ websearch_to_tsquery('english', :query)
                        THEN true
                        ELSE false
                    END AS abstractMatched,

                    CASE
                        WHEN to_tsvector(
                            'english',
                            coalesce(authors, '')
                        ) @@ websearch_to_tsquery('english', :query)
                        THEN true
                        ELSE false
                    END AS authorMatched,

                    CASE
                        WHEN to_tsvector(
                            'english',
                            coalesce(categories, '')
                        ) @@ websearch_to_tsquery('english', :query)
                        THEN true
                        ELSE false
                    END AS categoryMatched,

                    ts_headline(
                        'english',
                        title,
                        websearch_to_tsquery('english', :query),
                        'StartSel=<mark>, StopSel=</mark>, MaxFragments=1, MaxWords=30, MinWords=10'
                    ) AS highlightedTitle,

                    ts_headline(
                        'english',
                        abstract_text,
                        websearch_to_tsquery('english', :query),
                        'StartSel=<mark>, StopSel=</mark>, MaxFragments=2, MaxWords=60, MinWords=20'
                    ) AS highlightedAbstract

                FROM local_papers

                WHERE search_vector @@ websearch_to_tsquery(
                    'english',
                    :query
                )

                AND (
                    CAST(:category AS text) IS NULL
                    OR categories ILIKE CONCAT(
                        '%',
                        CAST(:category AS text),
                        '%'
                    )
                )

                AND (
                    CAST(:fromDate AS date) IS NULL
                    OR published_at >= CAST(:fromDate AS date)
                )

                AND (
                    CAST(:toDate AS date) IS NULL
                    OR published_at < CAST(:toDate AS date) + INTERVAL '1 day'
                )

                ORDER BY

                    CASE
                        WHEN :sort = 'newest'
                        THEN EXTRACT(EPOCH FROM published_at)
                    END DESC,

                    CASE
                        WHEN :sort = 'oldest'
                        THEN EXTRACT(EPOCH FROM published_at)
                    END ASC,

                    CASE
                        WHEN :sort = 'relevance'
                        THEN
                            ts_rank(
                                search_vector,
                                websearch_to_tsquery('english', :query),
                                32
                            )
                            +
                            2.0 * ts_rank(
                                to_tsvector(
                                    'english',
                                    coalesce(title, '')
                                ),
                                websearch_to_tsquery('english', :query),
                                32
                            )
                    END DESC,

                    published_at DESC
                """,

            countQuery = """
                SELECT COUNT(*)
                FROM local_papers

                WHERE search_vector @@ websearch_to_tsquery(
                    'english',
                    :query
                )

                AND (
                    CAST(:category AS text) IS NULL
                    OR categories ILIKE CONCAT(
                        '%',
                        CAST(:category AS text),
                        '%'
                    )
                )

                AND (
                    CAST(:fromDate AS date) IS NULL
                    OR published_at >= CAST(:fromDate AS date)
                )

                AND (
                    CAST(:toDate AS date) IS NULL
                    OR published_at < CAST(:toDate AS date) + INTERVAL '1 day'
                )
                """,

            nativeQuery = true
    )
    Page<LocalPaperSearchResult> searchFullText(
            @Param("query") String query,
            @Param("category") String category,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("sort") String sort,
            Pageable pageable
    );
}