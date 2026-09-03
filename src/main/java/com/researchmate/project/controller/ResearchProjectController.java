package com.researchmate.project.controller;


import com.researchmate.project.dto.CreateProjectRequest;
import com.researchmate.project.dto.ProjectResponse;
import com.researchmate.project.dto.UpdateProjectRequest;
import com.researchmate.project.repository.ResearchProjectRepository;
import com.researchmate.project.service.ResearchProjectService;
import com.researchmate.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ResearchProjectController {
    private final ResearchProjectService researchProjectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request, Authentication authentication){

        ProjectResponse response=researchProjectService.createProject(request,authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Authentication authentication){
        List<ProjectResponse> projects=researchProjectService.getMyProjects(authentication.getName());
        return ResponseEntity.ok(projects);
    }
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID projectId,Authentication authentication){
         ProjectResponse project=researchProjectService.getProject(projectId,authentication.getName());
         return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable UUID projectId,@Valid @RequestBody UpdateProjectRequest request,Authentication authentication){
        ProjectResponse project=researchProjectService.updateProject(projectId,request,authentication.getName());
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        researchProjectService.deleteProject(
                projectId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}
