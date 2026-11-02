package com.researchmate.chat.repository;

import com.researchmate.chat.entity.MessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageSourceRepository
        extends JpaRepository<MessageSource, UUID> {

    List<MessageSource> findAllByMessage_Id(UUID messageId);
}