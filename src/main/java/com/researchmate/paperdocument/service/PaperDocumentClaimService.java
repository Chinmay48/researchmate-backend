package com.researchmate.paperdocument.service;

import com.researchmate.paperdocument.repository.PaperDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaperDocumentClaimService {

    private final PaperDocumentRepository documentRepository;
    @Value("${researchmate.documents.max-attempts:3}")
    private int maxAttempts;
    @Transactional
    public boolean claimDocument(UUID documentId) {
        return documentRepository.claimPendingDocument(documentId) == 1;
    }
    @Transactional
    public int recoverStaleDocuments() {
        return documentRepository.recoverStaleDocuments(maxAttempts);
    }
    @Transactional
    public boolean recordFailure(
            UUID documentId,
            int maxAttempts,
            String error
    ) {
        return documentRepository.recordFailure(
                documentId,
                maxAttempts,
                error
        ) == 1;
    }
}