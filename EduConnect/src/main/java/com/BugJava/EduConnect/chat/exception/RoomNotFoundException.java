package com.BugJava.EduConnect.chat.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException() {
        super("해당 코드를 가진 채팅방을 찾을 수 없습니다.");
    }
    public RoomNotFoundException(String message) {super(message);}
}
