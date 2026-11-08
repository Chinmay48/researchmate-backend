package com.researchmate.chat.dto;

import java.util.List;
import java.util.UUID;

public record ChatPaperSelectionResponse(
        UUID chatId,
        List<SelectedPaper> papers
) {
    public record SelectedPaper(
            UUID paperId,
            String title,
            String externalId
    ) {}
}