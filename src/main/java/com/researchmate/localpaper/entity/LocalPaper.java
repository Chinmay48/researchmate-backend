package com.researchmate.localpaper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "local_papers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_local_papers_source_external_id",
                        columnNames = {"source", "external_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_local_papers_external_id",
                        columnList = "external_id"
                ),
                @Index(
                        name = "idx_local_papers_source",
                        columnList = "source"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Identifier from the imported dataset.
     *
     * Examples:
     * - arXiv identifier
     * - Semantic Scholar corpus identifier
     * - Dataset-specific paper identifier
     */
    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    /**
     * Dataset or provider from which the paper originated.
     *
     * Examples:
     * - arxiv
     * - semantic-scholar-dataset
     * - custom-ai-dataset
     */
    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

    /**
     * Categories can initially be stored as a comma-separated string.
     * We can normalize this later if category filtering requires it.
     */
    @Column(name = "categories", columnDefinition = "TEXT")
    private String categories;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "paper_url", columnDefinition = "TEXT")
    private String paperUrl;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}