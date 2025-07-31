package com.BugJava.EduConnect.freeboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FbCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 100, message = "작성자는 100자 이하로 입력해야 합니다.")
    private String author;
}
