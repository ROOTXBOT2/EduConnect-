package com.BugJava.EduConnect.assignment.dto;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AssignmentCommentResponse> comments;

    public static AssignmentResponse from(Assignment assignment) {
        List<AssignmentCommentResponse> commentResponses = assignment.getComments().stream()
                .filter(comment -> comment.getParent() == null)
                .map(AssignmentCommentResponse::fromWithChildren)
                .collect(Collectors.toList());

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .author(assignment.getUser().getName())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .comments(commentResponses)
                .build();
    }

    public static AssignmentResponse fromWithoutComments(Assignment assignment){
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .author(assignment.getUser().getName())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
