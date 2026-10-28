package com.researchmate.paperdocument.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfTextExtractionService {

    @Value("${researchmate.documents.max-pages:500}")
    private int maxPages;

    public List<ExtractedPage> extractPages(Path pdfPath) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {

            int pageCount = document.getNumberOfPages();

            if (pageCount == 0) {
                throw new IOException("PDF contains no pages");
            }

            if (pageCount > maxPages) {
                throw new IOException(
                        "PDF exceeds the maximum allowed page count of "
                                + maxPages
                );
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            List<ExtractedPage> extractedPages =
                    new ArrayList<>(pageCount);

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String text = stripper.getText(document).trim();

                if (!text.isBlank()) {
                    extractedPages.add(new ExtractedPage(page, text));
                }
            }

            if (extractedPages.isEmpty()) {
                throw new IOException(
                        "No extractable text found in the PDF"
                );
            }

            return extractedPages;
        }
    }

    public record ExtractedPage(int pageNumber, String text) {
    }
}