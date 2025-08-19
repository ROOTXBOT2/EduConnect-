package com.BugJava.EduConnect.common.handler;

import com.BugJava.EduConnect.auth.exception.*;
import com.BugJava.EduConnect.chat.exception.*;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
import com.BugJava.EduConnect.freeboard.exception.CommentNotFoundException;
import java.util.stream.Collectors;

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



    // freeboard 모듈 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage, "VALIDATION_FAILED"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "ACCESS_DENIED"));
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handlePostNotFoundException(PostNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "POST_NOT_FOUND"));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleCommentNotFoundException(CommentNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "COMMENT_NOT_FOUND"));
    }



    // Chat 모듈 예외 처리
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleRoomNotFoundException(RoomNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "ROOM_NOT_FOUND"));
    }

    @ExceptionHandler(ChatSessionNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleChatSessionNotFoundException(ChatSessionNotFoundException ex) {
        return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage(), "CHAT_SESSION_NOT_FOUND"));
    }

    @ExceptionHandler(SessionNotStartedException.class)
    public ResponseEntity<ApiResponse<?>> handleSessionNotStartedException(SessionNotStartedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "SESSION_NOT_STARTED"));
    }

    @ExceptionHandler(ChatMessageNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleChatMessageNotFoundException(ChatMessageNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "CHAT_MESSAGE_NOT_FOUND"));
    }

    @ExceptionHandler(UnauthorizedMessageAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedMessageAccessException(UnauthorizedMessageAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "UNAUTHORIZED_MESSAGE_ACCESS"));
    }

    @ExceptionHandler(AlreadyEnrolledException.class)
    public ResponseEntity<ApiResponse<?>> handleAlreadyEnrolledException(AlreadyEnrolledException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "ALREADY_ENROLLED"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException ex) {
        // EnrollmentService에서 이미 참여 중인 경우 등
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "CONFLICT_STATE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_ARGUMENT"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAll(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("알 수 없는 서버 오류: " + ex.getMessage(),"UNKNOWN_ERROR"));
    }
}