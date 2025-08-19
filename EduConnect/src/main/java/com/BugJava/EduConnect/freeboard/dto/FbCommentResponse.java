package com.BugJava.EduConnect.freeboard.dto;

import com.BugJava.EduConnect.freeboard.domain.FbComment;
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
    private String authorName;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // private Long postId; // 어떤 게시글에 달린 댓글인지 표시 - 제거됨

    public static FbCommentResponse from(FbComment comment) {
        return FbCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getUser().getName())
                .userId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                // .postId(comment.getPost().getId()) // 제거됨
                .build();
    }
}