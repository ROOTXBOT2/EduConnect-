package com.BugJava.EduConnect.freeboard.controller;

import com.BugJava.EduConnect.common.dto.ApiResponse;
import com.BugJava.EduConnect.common.dto.PageResponse;
import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.service.FbCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class FbCommentController {

    private final FbCommentService commentService;

    /** 댓글 목록 조회 (페이징, 정렬 포함) */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FbCommentResponse>>> getCommentsByPostId(
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponse<FbCommentResponse> comments = commentService.getCommentsByPostId(postId, pageable);
        return ResponseEntity.ok(ApiResponse.success(comments, "댓글 목록 조회 성공"));
    }

    /** 댓글 생성 */
    @PostMapping
    public ResponseEntity<ApiResponse<FbCommentResponse>> createComment(@PathVariable Long postId,
                                                                        @RequestBody @Valid FbCommentRequest request,
                                                                        @AuthenticationPrincipal Long userId) {
        FbCommentResponse created = commentService.createComment(postId, request, userId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.success(created, "댓글 생성 성공"));
    }

    /** 댓글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FbCommentResponse>> updateComment(@PathVariable Long postId,
                                                                        @PathVariable(name = "id") Long id,
                                                                        @RequestBody @Valid FbCommentRequest request) {
        FbCommentResponse updated = commentService.updateComment(postId, id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "댓글 수정 성공"));
    }

    /** 댓글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteComment(@PathVariable Long postId,
                                                             @PathVariable(name = "id") Long id) {
        commentService.deleteComment(postId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글 삭제 성공"));
    }
}