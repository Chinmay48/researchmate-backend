package com.researchmate.chat.repository;

import com.researchmate.chat.entity.ChatPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatPaperRepository extends JpaRepository<ChatPaper, UUID> {

    List<ChatPaper> findAllByChat_Id(UUID chatId);

    void deleteAllByChat_Id(UUID chatId);
    List<ChatPaper> findAllByChat_IdOrderByCreatedAtAsc(UUID chatId);
}