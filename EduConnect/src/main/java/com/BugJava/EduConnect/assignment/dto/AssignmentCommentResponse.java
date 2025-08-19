package com.BugJava.EduConnect.assignment.dto;

import com.BugJava.EduConnect.assignment.domain.AssignmentComment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentCommentResponse {
    private Long id;
    private Long postId;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentId;
    private List<AssignmentCommentResponse> children;

    public static AssignmentCommentResponse from(AssignmentComment comment){

        return AssignmentCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getAssignment().getId())
                .content(comment.getContent())
                .author(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .build();
    }

    // 트리 구조로 children 재귀 조립하는 메서드
    public static AssignmentCommentResponse fromWithChildren(AssignmentComment comment) {
        List<AssignmentCommentResponse> childResponses = comment.getChildren().stream()
                .map(AssignmentCommentResponse::fromWithChildren)  // 재귀 호출
                .collect(Collectors.toList());

        return AssignmentCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getAssignment().getId())
                .content(comment.getContent())
                .author(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .children(childResponses)
                .build();
    }
}
