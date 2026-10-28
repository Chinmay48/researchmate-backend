package com.researchmate.paperdocument.repository;

import com.researchmate.paperdocument.entity.PaperChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaperChunkRepository
        extends JpaRepository<PaperChunk, UUID> {

    List<PaperChunk> findAllByPaperDocument_IdOrderByChunkIndexAsc(
            UUID paperDocumentId
    );

    boolean existsByPaperDocument_Id(UUID paperDocumentId);

    long countByPaperDocument_Id(UUID paperDocumentId);

    void deleteAllByPaperDocument_Id(UUID paperDocumentId);
}