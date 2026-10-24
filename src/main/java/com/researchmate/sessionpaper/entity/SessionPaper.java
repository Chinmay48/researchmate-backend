package com.researchmate.sessionpaper.entity;

import com.researchmate.localpaper.entity.LocalPaper;
import com.researchmate.session.entity.ResearchSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "session_papers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_session_paper",
                        columnNames = {"session_id", "paper_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ResearchSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paper_id", nullable = false)
    private LocalPaper paper;

    @Column(nullable = false)
    private Integer rank;

    private Double keywordScore;

    private Double semanticScore;

    private Double hybridScore;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}