package com.BugJava.EduConnect.chat.domain;

import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatCodeBlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage chatMessage;

    private String language;

    @Lob
    private String codeContent;

    @Builder
    public ChatCodeBlock(ChatMessage chatMessage, String language, String codeContent) {
        this.chatMessage = chatMessage;
        this.language = language;
        this.codeContent = codeContent;
    }

    public void updateCodeContent(String newCodeContent) {
        this.codeContent = newCodeContent;
    }

    public void updateLanguage(String newLanguage) {
        this.language = newLanguage;
    }
}
