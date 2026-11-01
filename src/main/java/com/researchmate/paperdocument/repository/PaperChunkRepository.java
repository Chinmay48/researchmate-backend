package com.researchmate.paperdocument.repository;

import com.researchmate.paperdocument.entity.PaperChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.researchmate.paperdocument.projection.RetrievedChunk;

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
    @Modifying
    @Query(value = """
        UPDATE paper_chunks
        SET embedding = CAST(:embedding AS vector)
        WHERE id = :chunkId
        """, nativeQuery = true)
    int updateEmbedding(
            @Param("chunkId") UUID chunkId,
            @Param("embedding") String embedding
    );

    @Query(value = """
        SELECT
            pc.id AS id,
           pd.paper_id AS "paperId",
           pc.chunk_index AS "chunkIndex",
           pc.page_number AS "pageNumber",
           pc.section_title AS "sectionTitle",
            pc.content AS content,
            (pc.embedding <=> CAST(:queryEmbedding AS vector)) AS distance
        FROM paper_chunks pc
        JOIN paper_documents pd
            ON pd.id = pc.paper_document_id
        WHERE pd.status = 'COMPLETED'
          AND pc.embedding IS NOT NULL
        ORDER BY pc.embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<RetrievedChunk> findSimilarChunks(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT
            pc.id AS id,
            pd.paper_id AS "paperId",
            pc.chunk_index AS "chunkIndex",
            pc.page_number AS "pageNumber",
            pc.section_title AS "sectionTitle",
            pc.content AS content,
            (pc.embedding <=> CAST(:queryEmbedding AS vector)) AS distance
        FROM paper_chunks pc
        JOIN paper_documents pd
            ON pd.id = pc.paper_document_id
        WHERE pd.status = 'COMPLETED'
          AND pc.embedding IS NOT NULL
          AND pd.paper_id IN (:paperIds)
        ORDER BY pc.embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<RetrievedChunk> findSimilarChunksInPapers(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("paperIds") List<UUID> paperIds,
            @Param("limit") int limit
    );
}