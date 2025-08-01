package com.BugJava.EduConnect.freeboard.controller;

import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.service.FbCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class FbCommentController {

    private final FbCommentService commentService;

    /** 단일 댓글 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<FbCommentResponse> getComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getComment(id));
    }

    /** 댓글 생성 */
    @PostMapping
    public ResponseEntity<FbCommentResponse> createComment(@PathVariable Long postId,
                                                           @RequestBody @Valid FbCommentRequest request,
                                                           @AuthenticationPrincipal Long userId) {
        FbCommentResponse created = commentService.createComment(postId, request, userId);
        return ResponseEntity.ok(created); // created(location) 대신 ok 사용
    }

    /** 댓글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<FbCommentResponse> updateComment(@PathVariable Long id,
                                                           @RequestBody @Valid FbCommentRequest request,
                                                           @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(commentService.updateComment(id, request, userId));
    }

    /** 댓글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id,
                                          @AuthenticationPrincipal Long userId) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
}

