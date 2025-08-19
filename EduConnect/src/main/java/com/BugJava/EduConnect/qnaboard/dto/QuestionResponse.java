package com.BugJava.EduConnect.qnaboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author rua
 */
@Getter @Builder @AllArgsConstructor
@NoArgsConstructor

public class QuestionResponse {
    private Long id;
    private String title;
    private String content;
    private String writerName;
    private String startTrack;
    private String endTrack;
    private LocalDateTime createdAt;
    private Long answerCount;
    private Long commentCount;
}