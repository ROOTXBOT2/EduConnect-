package com.BugJava.EduConnect.chat.domain;

import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage chatMessage;

    private String fileName;

    private String fileUrl;

    @Builder
    public ChatAttachment(ChatMessage chatMessage, String fileName, String fileUrl) {
        this.chatMessage = chatMessage;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
