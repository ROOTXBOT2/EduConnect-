package com.BugJava.EduConnect.chat.dto;

import com.BugJava.EduConnect.chat.enums.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponse {

    private Long messageId;
    private Long sessionId;
    private Long senderId;
    private String sender;
    private String content;
    private MessageType messageType;
    private LocalDateTime timestamp;
    private boolean edited;
    private boolean deleted;

    // for code block
    private String language;
    private String codeContent;

    // for attachments
    private String fileName;
    private String fileUrl;

    @Builder
    public MessageResponse(Long messageId, Long sessionId, Long senderId, String sender, String content, MessageType messageType, LocalDateTime timestamp, boolean edited, boolean deleted, String language, String codeContent, String fileName, String fileUrl) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.senderId = senderId;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.edited = edited;
        this.deleted = deleted;
        this.language = language;
        this.codeContent = codeContent;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
