package com.researchmate.localpaper.dto;

import java.time.LocalDate;

public record LocalPaperSearchRequest(
        String query,
        String category,
        LocalDate fromDate,
        LocalDate toDate,
        String sort,
        int page,
        int size
) {
}
