package com.BugJava.EduConnect.chat.exception;

public class SessionNotStartedException extends RuntimeException {
    public SessionNotStartedException() {
        super("아직 세션이 시작되지 않았습니다.");
    }
}
