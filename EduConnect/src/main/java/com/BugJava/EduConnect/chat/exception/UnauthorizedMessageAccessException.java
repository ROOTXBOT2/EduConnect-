package com.BugJava.EduConnect.chat.exception;

public class UnauthorizedMessageAccessException extends RuntimeException {
    public UnauthorizedMessageAccessException() {
        super("메시지에 대한 권한이 없습니다.");
    }

    public UnauthorizedMessageAccessException(String message) {
        super(message);
    }
}
