
package com.researchmate.sessionpaper.service;

import com.researchmate.localpaper.dto.LocalPaperResponse;
import com.researchmate.localpaper.dto.LocalPaperSemanticSearchResult;
import com.researchmate.localpaper.entity.LocalPaper;
import com.researchmate.localpaper.repository.LocalPaperRepository;
import com.researchmate.localpaper.service.LocalPaperHybridSearchService;
import com.researchmate.localpaper.service.LocalPaperSearchService;
import com.researchmate.localpaper.service.LocalPaperSemanticSearchService;
import com.researchmate.session.entity.ResearchSession;
import com.researchmate.session.entity.SearchType;
import com.researchmate.sessionpaper.entity.SessionPaper;
import com.researchmate.sessionpaper.repository.SessionPaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionPaperRetrievalService {

    private static final int RESULT_LIMIT = 20;

    private final LocalPaperSearchService keywordSearchService;
    private final LocalPaperSemanticSearchService semanticSearchService;
    private final LocalPaperHybridSearchService hybridSearchService;
    private final LocalPaperRepository localPaperRepository;
    private final SessionPaperRepository sessionPaperRepository;

    @Transactional
    public int retrieveAndPersist(ResearchSession session) {
        List<SessionPaper> results = switch (session.getSearchType()) {
            case KEYWORD -> retrieveKeyword(session);
            case SEMANTIC -> retrieveSemantic(session);
            case HYBRID -> retrieveHybrid(session);
        };

        sessionPaperRepository.saveAll(results);
        return results.size();
    }

    private List<SessionPaper> retrieveKeyword(ResearchSession session) {
        Page<LocalPaperResponse> page = keywordSearchService.search(
                session.getQuery(),
                null,
                null,
                null,
                "relevance",
                0,
                RESULT_LIMIT
        );

        List<LocalPaperResponse> papers = page.getContent();

        Map<UUID, LocalPaper> entities = loadPapers(
                papers.stream().map(LocalPaperResponse::id).toList()
        );

        return java.util.stream.IntStream.range(0, papers.size())
                .mapToObj(index -> {
                    LocalPaperResponse result = papers.get(index);
                    LocalPaper paper = requirePaper(entities, result.id());

                    return SessionPaper.builder()
                            .session(session)
                            .paper(paper)
                            .rank(index + 1)
                            .keywordScore(result.relevanceScore())
                            .build();
                })
                .toList();
    }

    private List<SessionPaper> retrieveSemantic(ResearchSession session) {
        Page<LocalPaperSemanticSearchResult> page =
                semanticSearchService.search(session.getQuery(), 0, RESULT_LIMIT);

        return mapSemanticResults(session, page.getContent());
    }

    private List<SessionPaper> retrieveHybrid(ResearchSession session) {
        Page<LocalPaperSemanticSearchResult> page =
                hybridSearchService.search(session.getQuery(), 0, RESULT_LIMIT);

        return mapSemanticResults(session, page.getContent());
    }

    private List<SessionPaper> mapSemanticResults(
            ResearchSession session,
            List<LocalPaperSemanticSearchResult> results
    ) {
        Map<UUID, LocalPaper> entities = loadPapers(
                results.stream().map(LocalPaperSemanticSearchResult::id).toList()
        );

        return java.util.stream.IntStream.range(0, results.size())
                .mapToObj(index -> {
                    LocalPaperSemanticSearchResult result = results.get(index);
                    LocalPaper paper = requirePaper(entities, result.id());

                    return SessionPaper.builder()
                            .session(session)
                            .paper(paper)
                            .rank(index + 1)
                            .keywordScore(result.keywordScore())
                            .semanticScore(result.semanticScore())
                            .hybridScore(result.hybridScore())
                            .build();
                })
                .toList();
    }

    private Map<UUID, LocalPaper> loadPapers(List<UUID> ids) {
        Map<UUID, LocalPaper> entities = new HashMap<>();

        localPaperRepository.findAllById(ids)
                .forEach(paper -> entities.put(paper.getId(), paper));

        return entities;
    }

    private LocalPaper requirePaper(
            Map<UUID, LocalPaper> entities,
            UUID paperId
    ) {
        LocalPaper paper = entities.get(paperId);

        if (paper == null) {
            throw new IllegalStateException(
                    "Retrieved paper was not found in the database: " + paperId
            );
        }

        return paper;
    }
}
