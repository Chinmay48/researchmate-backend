package com.researchmate.localpaper.repository;

import com.researchmate.localpaper.entity.LocalPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
                SELECT *
                FROM local_papers
                WHERE search_vector @@ websearch_to_tsquery(
                        'english',
                        :query
                )
                ORDER BY ts_rank(
                        search_vector,
                        websearch_to_tsquery('english', :query)
                ) DESC
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM local_papers
                WHERE search_vector @@ websearch_to_tsquery(
                        'english',
                        :query
                )
                """,
            nativeQuery = true
    )
    Page<LocalPaper> searchFullText(
            @Param("query") String query,
            Pageable pageable
    );

}