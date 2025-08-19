package com.BugJava.EduConnect.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentCommentRequest {

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private Long parentId;  // 대댓글인 경우 부모 댓글 ID
}
