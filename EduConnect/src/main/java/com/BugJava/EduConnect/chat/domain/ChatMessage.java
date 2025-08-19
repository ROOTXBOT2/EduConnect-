package com.BugJava.EduConnect.chat.domain;

import com.BugJava.EduConnect.chat.enums.MessageType;
import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import com.BugJava.EduConnect.auth.entity.Users;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatSession chatSession;

    @ManyToOne(fetch = FetchType.LAZY) // 변경
    @JoinColumn(name = "sender_id", nullable = false) // 변경
    private Users sender; // 변경

    @Lob
    private String content;

    private boolean edited = false;

    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Builder
    public ChatMessage(ChatSession chatSession, Users sender, String content, MessageType messageType) { // 변경
        this.chatSession = chatSession;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType;
    }

    public void update(String content) {
        this.content = content;
        this.edited = true;
    }

    public void delete() {
        this.deleted = true;
    }

    public void setChatCodeBlock(ChatCodeBlock chatCodeBlock) {
        this.chatCodeBlock = chatCodeBlock;
    }

    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ChatCodeBlock chatCodeBlock;

    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ChatAttachment chatAttachment;
}
