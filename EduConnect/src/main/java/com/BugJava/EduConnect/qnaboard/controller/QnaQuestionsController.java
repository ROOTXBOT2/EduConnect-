package com.BugJava.EduConnect.qnaboard.controller;

import com.BugJava.EduConnect.common.dto.ApiResponse;
import com.BugJava.EduConnect.qnaboard.dto.*;
import com.BugJava.EduConnect.qnaboard.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @author rua
 */
@RestController
@RequestMapping("/api/qna/questions")
@RequiredArgsConstructor
@Slf4j
public class QnaQuestionsController {
    final public QuestionService questionService;

    @GetMapping
    @Operation(summary = "QnA 전체 목록 조회")
    public ResponseEntity<ApiResponse<Page<QuestionAllResponse>>> getAllQuestions(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<QuestionAllResponse> result = questionService.getAllQuestions(pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "QnA 전체 목록 반환"));
    }

    @PostMapping("/search")
    @Operation(summary = "QnA 검색")
    public ResponseEntity<ApiResponse<Page<QuestionAllResponse>>> getQuestions(
            @Valid @RequestBody QuestionSearchRequest req,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<QuestionAllResponse> result = questionService.getSearchQuestions(req, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "QnA 리스트 반환"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "QnA 글 상세 조회(전체공개)")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionDetailById(@PathVariable Long id) {
        QuestionResponse body = questionService.getQuestionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(body, "QnA 상세 반환"));
    }

    @PostMapping
    @Operation(summary = "QnA 질문 등록")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody QuestionCreateRequest req,
            @AuthenticationPrincipal Long userId // JWT 필터에서 principal=Long
    ) {
        questionService.createQuestion(userId, req);
        return ResponseEntity.ok(ApiResponse.success(null, "QnA 등록 완료"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "QnA 질문 수정")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionUpdateRequest req,
            @AuthenticationPrincipal Long userId
    ) {
        questionService.updateQuestion(userId, id, req);
        return ResponseEntity.ok(ApiResponse.success(null, "QnA 수정 완료"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "QnA 질문 삭제(Soft)")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        questionService.deleteQuestion(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "QnA 삭제 완료"));
    }

}