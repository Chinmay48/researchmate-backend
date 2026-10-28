package com.researchmate.paperdocument.service;


import com.researchmate.exception.ResourceNotFoundException;
import com.researchmate.localpaper.repository.LocalPaperRepository;
import com.researchmate.paperdocument.entity.PaperDocument;
import com.researchmate.paperdocument.repository.PaperDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaperDocumentSchedulingService {
    private final LocalPaperRepository localPaperRepository;
    private final PaperDocumentRepository paperDocumentRepository;

    @Transactional
    public PaperDocument scheduleIfNeeded(UUID paperId){
        if(!localPaperRepository.existsById(paperId)){
            throw new ResourceNotFoundException("Paper not found");
        }
        paperDocumentRepository.insertIfAbsent(UUID.randomUUID(),paperId);
        return paperDocumentRepository.findByPaper_Id(paperId).orElseThrow(()->new IllegalStateException("Paper document record could not be created"));
    }

}
