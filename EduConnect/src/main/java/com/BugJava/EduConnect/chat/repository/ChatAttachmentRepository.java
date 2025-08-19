package com.BugJava.EduConnect.chat.repository;

import com.BugJava.EduConnect.chat.domain.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import com.BugJava.EduConnect.chat.domain.ChatMessage;
import java.util.Optional;

public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Long> {
    Optional<ChatAttachment> findByChatMessage(ChatMessage chatMessage);
}
