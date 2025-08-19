package com.BugJava.EduConnect.chat.dto;

import com.BugJava.EduConnect.chat.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size; // Size 어노테이션 임포트
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageRequest {

    @NotNull(message = "메세지 타입은 필수입니다.")
    private MessageType messageType;

    @NotBlank(message = "메세지 내용이 없습니다.")
    @Size(max = 1048576, message = "메시지 내용은 1MB(1,048,576자)를 초과할 수 없습니다.") // 1MB 제한
    private String content;

    @Size(max = 50, message = "언어 이름은 50자를 초과할 수 없습니다.") // 언어 이름 50자 제한
    private String language; // for code block
}
