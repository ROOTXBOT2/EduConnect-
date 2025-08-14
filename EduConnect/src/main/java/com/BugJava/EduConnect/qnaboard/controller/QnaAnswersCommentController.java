package com.BugJava.EduConnect.qnaboard.controller;

import com.BugJava.EduConnect.common.dto.ApiResponse;
import com.BugJava.EduConnect.qnaboard.dto.*;
import com.BugJava.EduConnect.qnaboard.service.AnswerService;
import com.BugJava.EduConnect.qnaboard.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author rua
 */
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
@Slf4j
public class QnaAnswersCommentController {

    private final AnswerService answerService;
    private final CommentService commentService;

    // Answer APIs
    @PostMapping("/questions/{questionId}/answers")
    @Operation(summary = "QnA 답변 등록")
    public ResponseEntity<ApiResponse<Void>> createAnswer(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AnswerCreateRequest req) {
        answerService.createAnswer(questionId, userId, req);
        return ResponseEntity.ok(ApiResponse.success(null, "답변 등록 완료"));
    }

    @GetMapping("/questions/{questionId}/answers")
    @Operation(summary = "QnA 답변 목록 조회")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> getAnswers(@PathVariable Long questionId) {
        List<AnswerResponse> result = answerService.getAnswers(questionId);
        return ResponseEntity.ok(ApiResponse.success(result, "답변 목록 반환"));
    }

    @PutMapping("/answers/{answerId}")
    @Operation(summary = "QnA 답변 수정")
    public ResponseEntity<ApiResponse<Void>> updateAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AnswerUpdateRequest req) {
        answerService.updateAnswer(answerId, userId, req);
        return ResponseEntity.ok(ApiResponse.success(null, "답변 수정 완료"));
    }

    @DeleteMapping("/answers/{answerId}")
    @Operation(summary = "QnA 답변 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal Long userId) {
        answerService.deleteAnswer(answerId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "답변 삭제 완료"));
    }

    // Comment APIs
    @PostMapping("/answers/{answerId}/comments")
    @Operation(summary = "QnA 댓글 등록")
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable Long answerId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentCreateRequest req) {
        commentService.createComment(answerId, userId, req);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글 등록 완료"));
    }

    @GetMapping("/answers/{answerId}/comments")
    @Operation(summary = "QnA 댓글 목록 조회")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long answerId) {
        List<CommentResponse> result = commentService.getComments(answerId);
        return ResponseEntity.ok(ApiResponse.success(result, "댓글 목록 반환"));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "QnA 댓글 수정")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentUpdateRequest req) {
        commentService.updateComment(commentId, userId, req);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글 수정 완료"));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "QnA 댓글 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글 삭제 완료"));
    }
}
