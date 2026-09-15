package com.researchmate.localpaper.repository;

import com.researchmate.localpaper.entity.LocalPaper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    Page<LocalPaper> findByTitleContainingIgnoreCaseOrAbstractTextContainingIgnoreCase(
            String title,
            String abstractText,
            Pageable pageable
    );
}