package com.BugJava.EduConnect.qnaboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author rua
 */
@Getter
@Builder
public class AnswerListItem {
    private Long id;
    private String content;
    private String writerName;
    private LocalDateTime createdAt;
    private int commentCount;
}