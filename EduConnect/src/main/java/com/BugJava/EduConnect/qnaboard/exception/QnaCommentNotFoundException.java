package com.BugJava.EduConnect.qnaboard.exception;

public class QnaCommentNotFoundException extends RuntimeException {
    public QnaCommentNotFoundException(String message) {
        super("해당 댓글을 찾을 수 없습니다: " + message);
    }
}
