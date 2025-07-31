package com.BugJava.EduConnect.freeboard.exception;

public class FbCommentNotFoundException extends RuntimeException {
    public FbCommentNotFoundException() {
        super("댓글을 찾을 수 없습니다.");
    }

    public FbCommentNotFoundException(Long id) {
        super("ID가 " + id + "인 댓글을 찾을 수 없습니다.");
    }
}

