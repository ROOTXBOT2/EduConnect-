package com.BugJava.EduConnect.common.handler;

import com.BugJava.EduConnect.auth.exception.*;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import com.BugJava.EduConnect.qnaboard.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
                .status(HttpStatus.UNAUTHORIZED) // 401
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
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(Exception ex) {
        return ResponseEntity
                .status(403)
                .body(ApiResponse.error(ex.getMessage(),"INVALID_ACCESS"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAll(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("알 수 없는 서버 오류: " + ex.getMessage(),"UNKNOWN_ERROR"));
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(QuestionNotFoundException ex) {
        return ResponseEntity
                .status(404)
                .body(ApiResponse.error(ex.getMessage(), "QUESTION_NOT_FOUND"));
    }

    @ExceptionHandler(AnswerNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAnswerNotFound(AnswerNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "ANSWER_NOT_FOUND"));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleCommentNotFound(CommentNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "COMMENT_NOT_FOUND"));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(ApiResponse.error(ex.getMessage(),"INVALID_ACCESS"));
    }

    // Bean Validation 오류 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().isEmpty() 
            ? "입력 데이터가 올바르지 않습니다" 
            : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(ApiResponse.error(message, "VALIDATION_ERROR"));
    }
}