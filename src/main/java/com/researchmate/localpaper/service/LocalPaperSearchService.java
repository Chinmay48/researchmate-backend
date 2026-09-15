package com.researchmate.localpaper.service;


import com.researchmate.localpaper.dto.LocalPaperResponse;
import com.researchmate.localpaper.entity.LocalPaper;
import com.researchmate.localpaper.repository.LocalPaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalPaperSearchService {
    private final LocalPaperRepository localPaperRepository;

    public Page<LocalPaperResponse> search(String query,int page, int size){
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
        Page<LocalPaper> papers =
                localPaperRepository.searchFullText(
                        normalizedQuery,
                        pageable
                );
        return papers.map(LocalPaperResponse::fromEntity);

    }
}
