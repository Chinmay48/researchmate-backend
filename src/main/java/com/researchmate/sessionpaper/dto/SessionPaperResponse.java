package com.researchmate.sessionpaper.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionPaperResponse (
    UUID id,
    String externalId,
    String title,
    String authors,
    String abstractText,
    String categories,
    Instant publishedAt,
    String paperUrl,
    String pdfUrl,
    Integer rank,
    Double keywordScore,
    Double semanticScore,
    Double hybridScore
    ){}
