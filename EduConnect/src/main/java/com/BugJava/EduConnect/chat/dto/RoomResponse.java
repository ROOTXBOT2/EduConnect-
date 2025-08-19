package com.BugJava.EduConnect.chat.dto;

import com.BugJava.EduConnect.chat.domain.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RoomResponse {

    private Long roomId;
    private String title;
    private String code;
    private String instructorName;
    private LocalDateTime createdAt;
    private Long todaySessionId; // 오늘의 세션 ID (있다면)
    private String todaySessionStatus; // "NONE", "ACTIVE", "CLOSED"

}
