package com.BugJava.EduConnect.chat.exception;

public class UnauthorizedRoomAccessException extends RuntimeException {
    public UnauthorizedRoomAccessException(String message) {
        super(message);
    }
}
