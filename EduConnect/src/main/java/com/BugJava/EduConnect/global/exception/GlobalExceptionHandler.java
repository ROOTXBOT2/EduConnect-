package com.BugJava.EduConnect.global.exception;

import com.BugJava.EduConnect.freeboard.exception.FbPostNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FbPostNotFoundException.class)
    public ResponseEntity<String> handlePostNotFound(FbPostNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // 다른 도메인 예외도 여기에 추가 가능
}

