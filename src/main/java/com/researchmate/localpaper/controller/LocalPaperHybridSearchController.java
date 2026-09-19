package com.researchmate.localpaper.controller;

import com.researchmate.localpaper.dto.LocalPaperSemanticSearchResult;
import com.researchmate.localpaper.service.LocalPaperHybridSearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/local-papers")
public class LocalPaperHybridSearchController {

    private final LocalPaperHybridSearchService service;

    public LocalPaperHybridSearchController(
            LocalPaperHybridSearchService service
    ) {
        this.service = service;
    }

    @GetMapping("/hybrid-search")
    public Page<LocalPaperSemanticSearchResult> hybridSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(query, page, size);
    }
}