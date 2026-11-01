package com.researchmate.sessionpaper.dto;

import java.util.UUID;

public record SessionRagChunkResponse(
        UUID chunkId,
        UUID paperId,
        Integer chunkIndex,
        Integer pageNumber,
        String sectionTitle,
        String content,
        Double distance
) {
}