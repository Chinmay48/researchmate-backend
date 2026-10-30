package com.researchmate.paperdocument.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class PaperDocumentSchedulingServiceTest {

    @Autowired
    private PaperDocumentSchedulingService schedulingService;

    @Test
    void scheduleTestPaper() {
        UUID paperId = UUID.fromString(
                "7a30a472-7bc9-4863-b551-b77b000d3e63"
        );

        var document = schedulingService.scheduleIfNeeded(paperId);

        System.out.println("Document ID: " + document.getId());
        System.out.println("Status: " + document.getStatus());
    }
}