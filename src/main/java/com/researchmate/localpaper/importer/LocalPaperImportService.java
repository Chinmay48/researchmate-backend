package com.researchmate.localpaper.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.researchmate.localpaper.entity.LocalPaper;
import com.researchmate.localpaper.repository.LocalPaperRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalPaperImportService {

    private final ObjectMapper objectMapper;
    private final LocalPaperRepository localPaperRepository;
    private final EntityManager entityManager;

    private static final int BATCH_SIZE = 500;

    @Transactional
    public ImportResult importFile(String filePath) throws IOException {

        Path inputPath = Path.of(filePath);

        if (!Files.exists(inputPath)) {
            throw new IOException("Input file does not exist: " + inputPath);
        }

        long totalLines = 0;
        long malformedLines = 0;
        long invalidRecords = 0;
        long duplicateRecords = 0;
        long importedRecords = 0;

        List<LocalPaper> batch = new ArrayList<>(BATCH_SIZE);

        System.out.println("Starting local-paper import...");
        System.out.println("Input file: " + inputPath);
        System.out.println("Batch size: " + BATCH_SIZE);

        try (BufferedReader reader = Files.newBufferedReader(
                inputPath,
                StandardCharsets.UTF_8
        )) {

            String line;

            while ((line = reader.readLine()) != null) {
                totalLines++;

                if (line.isBlank()) {
                    continue;
                }

                JsonNode node;

                try {
                    node = objectMapper.readTree(line);
                } catch (Exception exception) {
                    malformedLines++;
                    continue;
                }

                if (node == null || !node.isObject()) {
                    malformedLines++;
                    continue;
                }

                String externalId = textValue(node, "externalId");
                String source = textValue(node, "source");
                String title = textValue(node, "title");
                String abstractText = textValue(node, "abstractText");

                if (externalId == null
                        || source == null
                        || title == null
                        || abstractText == null
                        || title.isBlank()
                        || abstractText.isBlank()) {

                    invalidRecords++;
                    continue;
                }

                if (localPaperRepository.existsBySourceAndExternalId(
                        source,
                        externalId
                )) {
                    duplicateRecords++;
                    continue;
                }

                LocalPaper paper = LocalPaper.builder()
                        .externalId(externalId)
                        .source(source)
                        .title(title)
                        .authors(textValue(node, "authors"))
                        .abstractText(abstractText)
                        .categories(textValue(node, "categories"))
                        .publishedAt(parseInstant(node, "publishedAt"))
                        .updatedAt(parseInstant(node, "updatedAt"))
                        .paperUrl(textValue(node, "paperUrl"))
                        .pdfUrl(textValue(node, "pdfUrl"))
                        .createdAt(Instant.now())
                        .build();

                batch.add(paper);

                if (batch.size() >= BATCH_SIZE) {
                    localPaperRepository.saveAll(batch);
                    entityManager.flush();
                    entityManager.clear();

                    importedRecords += batch.size();
                    batch.clear();

                    if (importedRecords % 10_000 == 0) {
                        System.out.printf(
                                "Imported %,d records; read %,d lines%n",
                                importedRecords,
                                totalLines
                        );
                    }
                }
            }

            if (!batch.isEmpty()) {
                localPaperRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();

                importedRecords += batch.size();
                batch.clear();
            }
        }

        System.out.println();
        System.out.println("Local-paper import completed.");
        System.out.println("--------------------------------");
        System.out.printf("Lines read       : %,d%n", totalLines);
        System.out.printf("Malformed lines   : %,d%n", malformedLines);
        System.out.printf("Invalid records   : %,d%n", invalidRecords);
        System.out.printf("Duplicate records: %,d%n", duplicateRecords);
        System.out.printf("Imported records : %,d%n", importedRecords);

        return new ImportResult(
                totalLines,
                malformedLines,
                invalidRecords,
                duplicateRecords,
                importedRecords
        );
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            return null;
        }

        String text = value.asText();

        return text == null || text.isBlank()
                ? null
                : text.trim();
    }

    private Instant parseInstant(JsonNode node, String fieldName) {
        String value = textValue(node, fieldName);

        if (value == null) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            // The extractor may contain dates such as 2024-05-10.
        }

        try {
            return LocalDate.parse(value)
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) {
            return null;
        }
    }

    public record ImportResult(
            long totalLines,
            long malformedLines,
            long invalidRecords,
            long duplicateRecords,
            long importedRecords
    ) {
    }
}