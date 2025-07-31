package com.BugJava.EduConnect.common.handler;

import com.BugJava.EduConnect.auth.exception.*;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author rua
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    //이메일 중복
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(),"DUPLICATE_EMAIL")); // 표준화된 에러 구조
    }

    //리프레쉬 토큰 인증 오류
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(ApiResponse.error(ex.getMessage(), "REFRESH_TOKEN_INVALID"));
    }

    //Email,Password 오류
    @ExceptionHandler(InvalidEmailPasswordException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidEmailPassword(InvalidEmailPasswordException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_EMAIL_PASSWORD"));
    }

    //유저 정보 조회 오류
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "USER_NOT_FOUND"));
    }

    //엑세스 토큰 인증 오류
    @ExceptionHandler(InvalidAccessTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidAccessToken(InvalidAccessTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401
                .body(ApiResponse.error(ex.getMessage(), "INVALID_ACCESS_TOKEN"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAll(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("알 수 없는 서버 오류: " + ex.getMessage(),"UNKNOWN_ERROR"));
    }
}