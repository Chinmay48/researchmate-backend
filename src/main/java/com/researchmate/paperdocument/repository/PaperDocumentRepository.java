package com.researchmate.paperdocument.repository;

import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.entity.PaperProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}