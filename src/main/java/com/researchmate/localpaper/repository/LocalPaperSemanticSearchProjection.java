package com.researchmate.localpaper.repository;

import java.time.Instant;
import java.util.UUID;

public interface LocalPaperSemanticSearchProjection {

    UUID getId();
    String getExternalId();
    String getTitle();
    String getAuthors();
    String getAbstractText();
    String getCategories();
    Instant getPublishedAt();
    Instant getUpdatedAt();
    String getPaperUrl();
    String getPdfUrl();

    Double getSimilarityScore();
}