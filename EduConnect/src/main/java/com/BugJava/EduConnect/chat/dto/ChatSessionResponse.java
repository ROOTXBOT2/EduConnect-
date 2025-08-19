package com.BugJava.EduConnect.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatSessionResponse {

    private Long sessionId;
    private String title;
    private LocalDate sessionDate;
    private boolean active;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
