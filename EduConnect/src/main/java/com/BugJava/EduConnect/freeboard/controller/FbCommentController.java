package com.BugJava.EduConnect.freeboard.controller;

import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.service.FbCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class FbCommentController {

    private final FbCommentService commentService;

    /** 특정 게시글 댓글 전체 조회 */
    @GetMapping
    public ResponseEntity<List<FbCommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    /** 댓글 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<FbCommentResponse> getComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getComment(id));
    }

    /** 댓글 생성 */
    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long postId,
                                           @RequestBody @Valid FbCommentRequest request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("잘못된 요청입니다.");
            return ResponseEntity.badRequest().body(errorMessage);
        }

        FbCommentResponse created = commentService.createComment(postId, request);
        return ResponseEntity.created(URI.create("/api/posts/" + postId + "/comments/" + created.getId()))
                .body(created);
    }

    /** 댓글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id,
                                           @RequestBody @Valid FbCommentRequest request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("잘못된 요청입니다.");
            return ResponseEntity.badRequest().body(errorMessage);
        }

        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    /** 댓글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}

