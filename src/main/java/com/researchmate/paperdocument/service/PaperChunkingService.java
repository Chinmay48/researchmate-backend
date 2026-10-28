package com.researchmate.paperdocument.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaperChunkingService {

    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 150;

    public List<ChunkData> chunkPages(
            List<PdfTextExtractionService.ExtractedPage> pages
    ) {
        List<ChunkData> chunks = new ArrayList<>();
        int chunkIndex = 0;

        for (var page : pages) {
            String text = page.text().trim();

            if (text.isBlank()) {
                continue;
            }

            int start = 0;

            while (start < text.length()) {
                int end = Math.min(start + CHUNK_SIZE, text.length());

                // Prefer a word boundary when it is reasonably close.
                if (end < text.length()) {
                    int boundary = text.lastIndexOf(' ', end);

                    if (boundary > start + CHUNK_SIZE / 2) {
                        end = boundary;
                    }
                }

                String content = text.substring(start, end).trim();

                if (!content.isBlank()) {
                    chunks.add(new ChunkData(
                            chunkIndex++,
                            page.pageNumber(),
                            content
                    ));
                }

                if (end >= text.length()) {
                    break;
                }

                int nextStart = Math.max(start + 1, end - CHUNK_OVERLAP);

                // Ensure progress even if a boundary was found unusually early.
                start = nextStart;
            }
        }

        return chunks;
    }

    public record ChunkData(
            int chunkIndex,
            int pageNumber,
            String content
    ) {}
}