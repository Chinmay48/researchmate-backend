package com.researchmate.localpaper.controller;


import com.researchmate.localpaper.dto.LocalPaperResponse;
import com.researchmate.localpaper.service.LocalPaperSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/local-papers")
public class LocalPaperSearchController {
    private final LocalPaperSearchService localPaperSearchService;
    @GetMapping("/search")
    public Page<LocalPaperResponse> search(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return localPaperSearchService.search(
                query,
                category,
                fromDate,
                toDate,
                sort,
                page,
                size
        );
    }
}
