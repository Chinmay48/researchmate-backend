package com.researchmate.localpaper.controller;

import com.researchmate.localpaper.dto.LocalPaperSemanticSearchResult;
import com.researchmate.localpaper.entity.LocalPaper;
import com.researchmate.localpaper.service.LocalPaperSemanticSearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/local-papers")
public class LocalPaperSemanticSearchController {

    private final LocalPaperSemanticSearchService service;

    public LocalPaperSemanticSearchController(
            LocalPaperSemanticSearchService service
    ) {
        this.service = service;
    }

    @GetMapping("/semantic-search")
    public Page<LocalPaperSemanticSearchResult> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(query, page, size);
    }
}