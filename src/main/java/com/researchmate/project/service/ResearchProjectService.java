package com.researchmate.project.service;

import com.researchmate.project.dto.CreateProjectRequest;
import com.researchmate.project.dto.ProjectResponse;
import com.researchmate.project.entity.ResearchProject;
import com.researchmate.project.repository.ResearchProjectRepository;
import com.researchmate.user.entity.User;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResearchProjectService {
    private final ResearchProjectRepository researchProjectRepository;
    private final UserRepository userRepository;
    public ProjectResponse createProject(CreateProjectRequest request, String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found"));
        ResearchProject project=ResearchProject.builder()
                .title(request.title())
                .description(request.description())
                .researchQuestion(request.researchQuestion())
                .user(user)
                .build();
        ResearchProject savedProject=researchProjectRepository.save(project);
                return toResponse(savedProject);
    }
    private ProjectResponse toResponse(
            ResearchProject project
    ) {

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getResearchQuestion(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
    public List<ProjectResponse> getMyProjects(String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found"));
        return researchProjectRepository.findAllByUserId(user.getId()).stream().map(this::toResponse).toList();
    }

    public ProjectResponse getProject(UUID projectId,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("User not found"));

        ResearchProject project=researchProjectRepository.findByIdAndUserId(projectId,user.getId()).orElseThrow(()->new IllegalArgumentException("Project not found"));
        return toResponse(project);
    }
}
