package com.researchmate.session.service;

import com.researchmate.exception.ResourceNotFoundException;
import com.researchmate.project.entity.ResearchProject;
import com.researchmate.project.repository.ResearchProjectRepository;
import com.researchmate.session.dto.CreateSessionRequest;
import com.researchmate.session.dto.SessionResponse;
import com.researchmate.session.dto.UpdateSessionRequest;
import com.researchmate.session.entity.ResearchSession;
import com.researchmate.session.repository.ResearchSessionRepository;
import com.researchmate.user.entity.User;
import com.researchmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResearchSessionService {
    private final UserRepository userRepository;
    private final ResearchProjectRepository projectRepository;
    private final ResearchSessionRepository sessionRepository;

    public SessionResponse createSession(UUID projectId, CreateSessionRequest request,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));
        ResearchProject project=projectRepository.findByIdAndUserId(projectId,user.getId()).orElseThrow(()->new ResourceNotFoundException("Project not found"));
        ResearchSession researchSession=ResearchSession.builder().title(request.title()).query(request.query()).project(project).build();
        ResearchSession savedSession=sessionRepository.save(researchSession);
        return toResponse(savedSession);
    }

    public List<SessionResponse> getProjectSession(UUID projectId,String email){
        User user =userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        projectRepository.findByIdAndUserId(projectId,user.getId()).orElseThrow(()->new ResourceNotFoundException("Project not found"));
        return sessionRepository.findAllByProjectIdAndProjectUserId(projectId,user.getId()).stream().map(this::toResponse).toList();

    }

    public SessionResponse getSession(UUID projectId,UUID sessionId,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));
        ResearchSession session=sessionRepository.findByIdAndProjectIdAndProjectUserId(sessionId,projectId,user.getId()).orElseThrow(()->new ResourceNotFoundException("Session not found"));
        return toResponse(session);
    }

    public SessionResponse updateSession(UUID projectId, UUID sessionId, UpdateSessionRequest request, String email){
         User user= userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));
         ResearchSession session=sessionRepository.findByIdAndProjectIdAndProjectUserId(sessionId,projectId,user.getId()).orElseThrow(()->new ResourceNotFoundException("Research Session not found"));
         session.setTitle(request.title());
         session.setQuery(request.query());
         ResearchSession updatedSession=sessionRepository.save(session);
         return toResponse(updatedSession);

    }

    public void deleteSession(UUID projectId,UUID sessionId,String email){
        User user=userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User not found"));
        ResearchSession session=sessionRepository.findByIdAndProjectIdAndProjectUserId(sessionId,projectId,user.getId()).orElseThrow(()->new ResourceNotFoundException("Research Session not found"));
        sessionRepository.delete(session);
    }

    private SessionResponse toResponse(ResearchSession session){
        return new SessionResponse(
                session.getId(),
                session.getProject().getId(),
                session.getTitle(),
                session.getQuery(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

}
