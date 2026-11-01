package com.researchmate.sessionpaper.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdatePaperSelectionRequest(

        @NotNull(message = "Paper IDs cannot be null")
        List<@NotNull UUID> paperIds

) {
}