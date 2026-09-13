package com.researchmate.paper.dto;

import com.researchmate.mcp.model.AcademicPaper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperSearchResponse {

    private String query;

    private String providerUsed;

    private List<String> messages;

    private List<AcademicPaper> papers;
}