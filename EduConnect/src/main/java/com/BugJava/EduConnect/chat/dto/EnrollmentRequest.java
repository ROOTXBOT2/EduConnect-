package com.BugJava.EduConnect.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EnrollmentRequest {
    @NotBlank(message = "방 코드는 필수입니다.")
    @Size(max = 8, message = "방 코드는 8자를 초과할 수 없습니다.")
    private String roomCode;
}
