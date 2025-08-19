package com.BugJava.EduConnect.common.handler;

import com.BugJava.EduConnect.assignment.exception.AssignmentCommentNotFoundException;
import com.BugJava.EduConnect.assignment.exception.AssignmentNotFoundException;
import com.BugJava.EduConnect.auth.exception.*;
import com.BugJava.EduConnect.chat.exception.*;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import com.BugJava.EduConnect.freeboard.exception.FbCommentNotFoundException;
import com.BugJava.EduConnect.freeboard.exception.FbPostNotFoundException;
import com.BugJava.EduConnect.qnaboard.exception.AnswerNotFoundException;
import com.BugJava.EduConnect.qnaboard.exception.ForbiddenException;
import com.BugJava.EduConnect.qnaboard.exception.QnaCommentNotFoundException;
import com.BugJava.EduConnect.qnaboard.exception.QuestionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage, "VALIDATION_FAILED"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_ARGUMENT"));
    }

    // 401 Unauthorized
    @ExceptionHandler({InvalidAccessTokenException.class, InvalidEmailPasswordException.class, InvalidRefreshTokenException.class})
    public ResponseEntity<ApiResponse<?>> handleAuthExceptions(RuntimeException ex) {
        String errorCode = "UNAUTHORIZED";
        if (ex instanceof InvalidAccessTokenException) errorCode = "INVALID_ACCESS_TOKEN";
        if (ex instanceof InvalidEmailPasswordException) errorCode = "INVALID_EMAIL_PASSWORD";
        if (ex instanceof InvalidRefreshTokenException) errorCode = "REFRESH_TOKEN_INVALID";

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), errorCode));
    }

    // 403 Forbidden
    @ExceptionHandler({AccessDeniedException.class, ForbiddenException.class, SessionNotStartedException.class, UnauthorizedMessageAccessException.class, com.BugJava.EduConnect.qnaboard.exception.AccessDeniedException.class})
    public ResponseEntity<ApiResponse<?>> handleForbiddenExceptions(RuntimeException ex) {
        String errorCode = "FORBIDDEN";
        if (ex instanceof ForbiddenException) errorCode = "INVALID_ACCESS";
        if (ex instanceof SessionNotStartedException) errorCode = "SESSION_NOT_STARTED";
        if (ex instanceof UnauthorizedMessageAccessException) errorCode = "UNAUTHORIZED_MESSAGE_ACCESS";

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), errorCode));
    }

    // 404 Not Found
    @ExceptionHandler({UserNotFoundException.class, AssignmentNotFoundException.class, AssignmentCommentNotFoundException.class,
            FbPostNotFoundException.class, FbCommentNotFoundException.class, QuestionNotFoundException.class, AnswerNotFoundException.class,
            QnaCommentNotFoundException.class, RoomNotFoundException.class, ChatSessionNotFoundException.class, ChatMessageNotFoundException.class})
    public ResponseEntity<ApiResponse<?>> handleNotFoundExceptions(RuntimeException ex) {
        String errorCode = "NOT_FOUND";
        // 각 예외 타입에 따라 에러 코드를 구체적으로 설정할 수 있습니다.
        if (ex instanceof UserNotFoundException) errorCode = "USER_NOT_FOUND";
        if (ex instanceof AssignmentNotFoundException) errorCode = "ASSIGNMENT_NOT_FOUND";
        if (ex instanceof AssignmentCommentNotFoundException) errorCode = "ASSIGNMENT_COMMENT_NOT_FOUND";
        if (ex instanceof FbPostNotFoundException) errorCode = "FB_POST_NOT_FOUND";
        if (ex instanceof FbCommentNotFoundException) errorCode = "FB_COMMENT_NOT_FOUND";
        if (ex instanceof QuestionNotFoundException) errorCode = "QNA_QUESTION_NOT_FOUND";
        if (ex instanceof AnswerNotFoundException) errorCode = "QNA_ANSWER_NOT_FOUND";
        if (ex instanceof QnaCommentNotFoundException) errorCode = "QNA_COMMENT_NOT_FOUND";
        if (ex instanceof RoomNotFoundException) errorCode = "ROOM_NOT_FOUND";
        if (ex instanceof ChatSessionNotFoundException) errorCode = "CHAT_SESSION_NOT_FOUND";
        if (ex instanceof ChatMessageNotFoundException) errorCode = "CHAT_MESSAGE_NOT_FOUND";

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), errorCode));
    }

    // 409 Conflict
    @ExceptionHandler({DuplicateEmailException.class, AlreadyEnrolledException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<?>> handleConflictExceptions(RuntimeException ex) {
        String errorCode = "CONFLICT";
        if (ex instanceof DuplicateEmailException) errorCode = "DUPLICATE_EMAIL";
        if (ex instanceof AlreadyEnrolledException) errorCode = "ALREADY_ENROLLED";
        if (ex instanceof IllegalStateException) errorCode = "CONFLICT_STATE";

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), errorCode));
    }

    // 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAll(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("알 수 없는 서버 오류: " + ex.getMessage(), "UNKNOWN_ERROR"));
    }
}
