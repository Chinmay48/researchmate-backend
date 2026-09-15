package com.researchmate.localpaper.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "localpaper.import.enabled",
        havingValue = "true"
)
public class LocalPaperImportRunner implements CommandLineRunner {

    private final LocalPaperImportService importService;

    @Override
    public void run(String... args) throws Exception {

        String filePath = System.getProperty(
                "localpaper.import.file",
                "C:\\ResearchMateData\\researchmate_papers_200k.jsonl"
        );

        importService.importFile(filePath);
    }
}