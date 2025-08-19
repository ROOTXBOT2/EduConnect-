package com.BugJava.EduConnect.chat.exception;

public class ChatSessionClosedException extends RuntimeException {
    public ChatSessionClosedException() {
        super("채팅 세션이 종료되어 메시지 작업을 수행할 수 없습니다.");
    }

    public ChatSessionClosedException(String message) {
        super(message);
    }
}
