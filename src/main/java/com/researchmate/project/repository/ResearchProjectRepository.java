package com.researchmate.project.repository;

import com.researchmate.project.entity.ResearchProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResearchProjectRepository extends JpaRepository<ResearchProject, UUID> {
    List<ResearchProject> findAllByUserId(UUID userUd);
    Optional<ResearchProject> findByIdAndUserId(UUID projectId,UUID userId);
}
