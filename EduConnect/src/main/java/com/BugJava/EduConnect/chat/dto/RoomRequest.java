package com.BugJava.EduConnect.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoomRequest {
    @NotBlank(message = "강의 제목은 필수입니다.")
    @Size(max = 255, message = "강의 제목은 255자를 초과할 수 없습니다.")
    private String title;
}
