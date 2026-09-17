package com.researchmate.localpaper.service;


import com.researchmate.localpaper.dto.LocalPaperResponse;
import com.researchmate.localpaper.entity.LocalPaper;
import com.researchmate.localpaper.repository.LocalPaperRepository;
import com.researchmate.localpaper.repository.LocalPaperSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalPaperSearchService {
    private final LocalPaperRepository localPaperRepository;

    public Page<LocalPaperResponse> search(String query, String category, LocalDate fromDate,LocalDate toDate, String sort,int page,int size){
        if (sort == null || sort.isBlank()) {
            sort = "relevance";
        }

        if (!sort.equals("relevance")
                && !sort.equals("newest")
                && !sort.equals("oldest")) {
            throw new IllegalArgumentException(
                    "Sort must be relevance, newest, or oldest"
            );
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "fromDate cannot be after toDate"
            );
        }
        String normalizedQuery=query==null?"":query.trim();
        if(normalizedQuery.isBlank()){
            throw new IllegalArgumentException("Search query cannot be empty");

        }
        if(page<0){
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if(size<1 || size>100){
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        Pageable pageable= PageRequest.of(page,size);
        Page<LocalPaperSearchResult> papers =
                localPaperRepository.searchFullText(
                        normalizedQuery,
                        category,
                        fromDate,
                        toDate,
                        sort,
                        pageable
                );

        return papers.map(result ->
                new LocalPaperResponse(
                        result.getId(),
                        result.getExternalId(),
                        result.getSource(),
                        result.getTitle(),
                        result.getAuthors(),
                        result.getAbstractText(),
                        result.getCategories(),
                        result.getPublishedAt(),
                        result.getUpdatedAt(),
                        result.getPaperUrl(),
                        result.getPdfUrl(),
                        result.getRelevanceScore(),
                        result.getTitleMatched(),
                        result.getAbstractMatched(),
                        result.getAuthorMatched(),
                        result.getCategoryMatched(),
                        result.getHighlightedTitle(),
                        result.getHighlightedAbstract()
                )
        );


    }
}
