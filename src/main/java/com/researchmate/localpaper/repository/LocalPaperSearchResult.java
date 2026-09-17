package com.researchmate.localpaper.repository;

import java.time.Instant;
import java.util.UUID;

public interface LocalPaperSearchResult {

    UUID getId();

    String getExternalId();

    String getSource();

    String getTitle();

    String getAuthors();

    String getAbstractText();

    String getCategories();

    Instant getPublishedAt();

    Instant getUpdatedAt();

    String getPaperUrl();

    String getPdfUrl();

    Double getRelevanceScore();

    Boolean getTitleMatched();

    Boolean getAbstractMatched();

    Boolean getAuthorMatched();

    Boolean getCategoryMatched();
    String getHighlightedTitle();
    String getHighlightedAbstract();
}