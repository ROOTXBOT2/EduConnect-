package com.BugJava.EduConnect.qnaboard.exception;

public class AnswerNotFoundException extends RuntimeException {
    public AnswerNotFoundException(String message) {
        super("해당 답변을 찾을 수 없습니다: " + message);
    }
}
