package com.BugJava.EduConnect.chat.exception;

public class AlreadyEnrolledException extends RuntimeException {
    public AlreadyEnrolledException() {
        super("이미 해당 강의실에 참여하고 있습니다.");
    }

    public AlreadyEnrolledException(String message) {
        super(message);
    }
}
