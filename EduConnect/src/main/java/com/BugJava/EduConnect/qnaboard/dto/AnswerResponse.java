package com.BugJava.EduConnect.qnaboard.dto;

import com.BugJava.EduConnect.qnaboard.entity.Answer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnswerResponse {
    private Long id;
    private String content;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .writerName(answer.getUser().getName())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .build();
    }
}
