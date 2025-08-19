package com.BugJava.EduConnect.chat.domain;

import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession extends BaseEntity {

        @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room; // 방 번호

    private LocalDate sessionDate; // 세션 날짜

    private String title; // 제목

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Builder
    public ChatSession(Room room, String title) {
        this.room = room;
        this.sessionDate = LocalDate.now();
        this.title = title;
        this.status = SessionStatus.OPEN;
    }

    public void close() {
        this.status = SessionStatus.CLOSED;
        this.endTime = LocalDateTime.now();
    }

    public void reopen() {
        this.status = SessionStatus.OPEN;
        this.endTime = null;
    }
}
