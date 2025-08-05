package com.BugJava.EduConnect.assignment.dto;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import lombok.*;
import java.time.LocalDateTime;

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

    public static AssignmentResponse from(Assignment assignment) {
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
