package com.researchmate.paper.controller;


import com.researchmate.paper.dto.ResearchPaperResponse;
import com.researchmate.paper.dto.SearchPapersRequest;
import com.researchmate.paper.entity.ResearchPaper;
import com.researchmate.paper.service.ResearchPaperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/sessions/{sessionId}/papers")
@RequiredArgsConstructor
public class ResearchPaperController {
    private final ResearchPaperService researchPaperService;
    @PostMapping("/search")
    public ResponseEntity<List<ResearchPaperResponse>> searchandSavePapers(@PathVariable UUID projectId, @PathVariable UUID sessionId, @Valid @RequestBody SearchPapersRequest request, Authentication authentication){
        List<ResearchPaperResponse> response=researchPaperService.searchAndSavePapers(projectId,sessionId,request.query(),authentication.getName());
        return  ResponseEntity.ok(response);
    }
}
