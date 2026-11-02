package com.researchmate.chat.repository;

import com.researchmate.chat.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    List<Chat> findAllBySession_IdOrderByUpdatedAtDesc(UUID sessionId);

    Optional<Chat> findByIdAndSession_Id(
            UUID chatId,
            UUID sessionId
    );

    Optional<Chat> findByIdAndSession_Project_User_Id(
            UUID chatId,
            UUID userId
    );
}