package com.researchmate.paperdocument.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "paper_chunks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_paper_chunks_document_index",
                        columnNames = {"paper_document_id", "chunk_index"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_paper_chunks_document",
                        columnList = "paper_document_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paper_document_id", nullable = false)
    private PaperDocument paperDocument;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "section_title", length = 500)
    private String sectionTitle;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}