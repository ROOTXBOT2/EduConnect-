package com.BugJava.EduConnect.qnaboard.dto;

import com.BugJava.EduConnect.qnaboard.entity.Question;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author rua
 */
@Getter
@Builder
public class QuestionAllResponse {
    private Long id;
    private String title;
    private String writerName;   // q.user.name
    private String startTrack;   // 작성자 트랙
    private String endTrack;     // 질문 대상 트랙 (q.track)
    private LocalDateTime createdAt;

    public static QuestionAllResponse from(Question q) {
        return QuestionAllResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .writerName(q.getUser().getName())
                .startTrack(q.getUser().getTrack().name())
                .endTrack(q.getTrack().name())
                .createdAt(q.getCreatedAt())
                .build();
    }
}