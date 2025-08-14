package com.BugJava.EduConnect.qnaboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AnswerCreateRequest {
    @NotBlank(message = "답변 내용은 비워둘 수 없습니다.")
    private String content;
}
