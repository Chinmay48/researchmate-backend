package com.researchmate.paper.service;

import com.researchmate.exception.ResourceNotFoundException;
import com.researchmate.mcp.McpToolService;
import com.researchmate.mcp.model.ArxivPaper;
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
    private final McpToolService mcpToolService;

    public List<ResearchPaperResponse> searchAndSavePapers(UUID projectId,UUID sessionId,String query,String userEmail){
        User user =userRepository.findByEmail(userEmail).orElseThrow(()->new ResourceNotFoundException("User not found"));
        ResearchProject project=researchProjectRepository.findByIdAndUserId(projectId,user.getId()).orElseThrow(()-> new ResourceNotFoundException("Project not found"));
        ResearchSession session=researchSessionRepository.findByIdAndProjectId(sessionId,project.getId()).orElseThrow(()->new ResourceNotFoundException("Session not found"));
        List<ArxivPaper> arxivPapers=mcpToolService.searchArxiv(query);
        List<ResearchPaper> papersToSave=new ArrayList<>();
        for(ArxivPaper arxivPaper:arxivPapers){
            boolean alreadyExists=researchPaperRepository.existsBySessionIdAndArxivId(session.getId(),arxivPaper.getArxivId());
            if(alreadyExists) continue;
            ResearchPaper paper=ResearchPaper.builder()
                    .arxivId(arxivPaper.getArxivId())
                    .title(arxivPaper.getTitle())
                    .authors(String.join(", ",arxivPaper.getAuthors()))
                    .abstractText(arxivPaper.getAbstractText())
                    .publishedAt(arxivPaper.getPublishedAt())
                    .updatedAt(arxivPaper.getPublishedAt())
                    .updatedAt(arxivPaper.getUpdatedAt())
                    .categories(String.join(", ",arxivPaper.getCategories()))
                    .paperUrl(arxivPaper.getPaperUrl())
                    .session(session)
                    .build();
            papersToSave.add(paper);
        }
        List<ResearchPaper> savedPapers=researchPaperRepository.saveAll(papersToSave);
        return savedPapers.stream().map(this::toResponse).toList();
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
