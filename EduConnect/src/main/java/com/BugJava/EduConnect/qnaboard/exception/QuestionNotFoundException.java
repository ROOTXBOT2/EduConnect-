package com.BugJava.EduConnect.qnaboard.exception;

/**
 * @author rua
 */
public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String message) {
        super(message);
    }
}
