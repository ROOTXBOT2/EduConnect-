package com.BugJava.EduConnect.freeboard.exception;

public class FbPostNotFoundException extends RuntimeException {

    public FbPostNotFoundException() {
        super("게시글을 찾을 수 없습니다.");
    }

    public FbPostNotFoundException(Long id) {
        super("ID가 " + id + "인 게시글을 찾을 수 없습니다.");
    }
}
