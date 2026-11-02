package com.researchmate.ai.dto;

import com.researchmate.ai.enums.AnswerMode;

import java.util.List;
import java.util.UUID;

public record SessionRagAnswerResponse(

        String answer,

        AnswerMode answerMode,

        List<SourceCitation> sources

) {

    public record SourceCitation(
            UUID paperId,
            UUID chunkId,
            Integer pageNumber,
            String sectionTitle,
            Double distance
    ) {}
}