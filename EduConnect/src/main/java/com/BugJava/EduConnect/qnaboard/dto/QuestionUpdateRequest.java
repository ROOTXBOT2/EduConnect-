package com.BugJava.EduConnect.qnaboard.dto;

import com.BugJava.EduConnect.auth.enums.Track;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class QuestionUpdateRequest {

    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    private String title;

    @NotBlank(message = "내용은 비워둘 수 없습니다.")
    private String content;

    @NotNull(message = "트랙을 지정해야 합니다.")
    private Track track;
}