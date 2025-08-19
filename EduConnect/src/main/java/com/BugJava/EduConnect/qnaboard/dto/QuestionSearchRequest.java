package com.BugJava.EduConnect.qnaboard.dto;

import com.BugJava.EduConnect.auth.enums.Track;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @author rua
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionSearchRequest {
    @Schema(description = "검색할 트랙", example = "BACKEND")
    private Track track;

    @Schema(description = "검색 키워드", example = "Spring Boot")
    @Size(min = 2, message = "검색어는 최소 2자 이상이어야 합니다")
    private String keyword;
}
