package com.BugJava.EduConnect.post.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException() {
        super("게시글을 찾을 수 없습니다.");
    }

    public PostNotFoundException(Long id) {
        super("ID가 " + id + "인 게시글을 찾을 수 없습니다.");
    }
}
