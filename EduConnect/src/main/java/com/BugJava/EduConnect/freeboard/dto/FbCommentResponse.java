package com.BugJava.EduConnect.freeboard.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FbCommentResponse {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long postId; // 어떤 게시글에 달린 댓글인지 표시
}

