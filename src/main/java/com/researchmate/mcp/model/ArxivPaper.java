package com.researchmate.mcp.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArxivPaper {

    private String arxivId;

    private String title;

    private List<String> authors;

    private String abstractText;

    private LocalDateTime publishedAt;

    private LocalDateTime updatedAt;

    private List<String> categories;

    private String paperUrl;

    private String pdfUrl;
}