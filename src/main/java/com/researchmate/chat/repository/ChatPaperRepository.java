package com.researchmate.chat.repository;

import com.researchmate.chat.entity.ChatPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ChatPaperRepository extends JpaRepository<ChatPaper, UUID> {

    List<ChatPaper> findAllByChat_Id(UUID chatId);

    void deleteAllByChat_Id(UUID chatId);
    List<ChatPaper> findAllByChat_IdOrderByCreatedAtAsc(UUID chatId);
    @Query("""
    SELECT cp.paper.id
    FROM ChatPaper cp
    WHERE cp.chat.id = :chatId
""")
    List<UUID> findPaperIdsByChatId(@Param("chatId") UUID chatId);

}