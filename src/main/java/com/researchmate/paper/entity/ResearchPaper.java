package com.researchmate.paper.entity;

import com.researchmate.session.entity.ResearchSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "research_papers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_session_arxiv_id",
                        columnNames = {"session_id", "arxiv_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "arxiv_id", nullable = false, length = 100)
    private String arxivId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String authors;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

    private LocalDateTime publishedAt;

    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String categories;

    @Column(columnDefinition = "TEXT")
    private String paperUrl;

    @Column(columnDefinition = "TEXT")
    private String pdfUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ResearchSession session;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}