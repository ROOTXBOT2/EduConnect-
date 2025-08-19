package com.BugJava.EduConnect.chat.repository;

import com.BugJava.EduConnect.chat.domain.ChatCodeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import com.BugJava.EduConnect.chat.domain.ChatMessage;
import java.util.Optional;

public interface ChatCodeBlockRepository extends JpaRepository<ChatCodeBlock, Long> {
    Optional<ChatCodeBlock> findByChatMessage(ChatMessage chatMessage);
}
