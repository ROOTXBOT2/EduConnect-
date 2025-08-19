package com.BugJava.EduConnect.qnaboard.dto;

import com.BugJava.EduConnect.auth.enums.Track;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author rua
 */

@Getter
@Setter
@NoArgsConstructor
public class QuestionCreateRequest {
    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank @Size(max = 5000)
    private String content;

    @NotNull
    private Track track;
}
