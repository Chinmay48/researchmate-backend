package com.researchmate.session.entity;

import com.researchmate.paper.entity.ResearchPaper;
import com.researchmate.project.entity.ResearchProject;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="research_sessions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResearchSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false,length = 300)
    private String title;
    @Column(nullable = false,columnDefinition = "TEXT")
    private String query;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 30)
    @Builder.Default
    private SessionStatus status=SessionStatus.CREATED;
    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "project_id",nullable = false)
    private ResearchProject project;
    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ResearchPaper> papers = new ArrayList<>();

}
