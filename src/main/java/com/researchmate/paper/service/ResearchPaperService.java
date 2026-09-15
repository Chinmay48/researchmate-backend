package com.researchmate.paper.service;


import com.researchmate.mcp.model.AcademicPaper;
import com.researchmate.paper.dto.ResearchPaperResponse;
import com.researchmate.paper.entity.ResearchPaper;
import com.researchmate.paper.repository.ResearchPaperRepository;
import com.researchmate.project.entity.ResearchProject;
import com.researchmate.project.repository.ResearchProjectRepository;
import com.researchmate.session.entity.ResearchSession;
import com.researchmate.session.repository.ResearchSessionRepository;
import com.researchmate.user.entity.User;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResearchPaperService {
    private final UserRepository userRepository;
    private final ResearchPaperRepository researchPaperRepository;
    private final ResearchSessionRepository researchSessionRepository;
    private final ResearchProjectRepository researchProjectRepository;


    @Transactional
    public List<ResearchPaperResponse> searchAndSavePapers(
            UUID projectId,
            UUID sessionId,
            String query,
            String userEmail
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ResearchProject project = researchProjectRepository
                .findByIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ResearchSession session = researchSessionRepository
                .findByIdAndProjectId(sessionId, project.getId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        List<AcademicPaper> academicPapers =List.of();

        List<ResearchPaper> papersToSave = new ArrayList<>();

        for (AcademicPaper academicPaper : academicPapers) {

            boolean alreadyExists =
                    researchPaperRepository.existsBySessionIdAndArxivId(
                            session.getId(),
                            academicPaper.getExternalId()
                    );

            if (alreadyExists) {
                continue;
            }

            ResearchPaper paper = ResearchPaper.builder()
                    .arxivId(academicPaper.getExternalId())
                    .title(academicPaper.getTitle())
                    .authors(
                            academicPaper.getAuthors() == null
                                    ? ""
                                    : String.join(
                                    ", ",
                                    academicPaper.getAuthors()
                            )
                    )
                    .abstractText(academicPaper.getAbstractText())
                    .publishedAt(academicPaper.getPublishedAt())
                    .updatedAt(academicPaper.getUpdatedAt())
                    .categories(
                            academicPaper.getCategories() == null
                                    ? ""
                                    : String.join(
                                    ", ",
                                    academicPaper.getCategories()
                            )
                    )
                    .paperUrl(academicPaper.getPaperUrl())
                    .pdfUrl(academicPaper.getPdfUrl())
                    .session(session)
                    .build();

            papersToSave.add(paper);
        }

        List<ResearchPaper> savedPapers =
                researchPaperRepository.saveAll(papersToSave);

        return savedPapers.stream()
                .map(this::toResponse)
                .toList();
    }
    private ResearchPaperResponse toResponse(ResearchPaper paper) {

        return new ResearchPaperResponse(
                paper.getId(),
                paper.getSession().getId(),
                paper.getArxivId(),
                paper.getTitle(),
                paper.getAuthors(),
                paper.getAbstractText(),
                paper.getPublishedAt(),
                paper.getUpdatedAt(),
                paper.getCategories(),
                paper.getPaperUrl(),
                paper.getPdfUrl(),
                paper.getCreatedAt()
        );
    }
}
