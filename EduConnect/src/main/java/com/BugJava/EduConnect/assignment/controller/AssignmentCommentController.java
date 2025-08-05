package com.BugJava.EduConnect.assignment.controller;

import com.BugJava.EduConnect.assignment.dto.AssignmentCommentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentCommentResponse;
import com.BugJava.EduConnect.assignment.service.AssignmentCommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/assignments/{assignmentId}/comments")
public class AssignmentCommentController {

    private final AssignmentCommentService assignmentCommentService;

    //댓글 생성
    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long assignmentId, @Valid @RequestBody AssignmentCommentRequest request,
                                              @AuthenticationPrincipal Long userId){
        AssignmentCommentResponse created = assignmentCommentService.createComment(request, userId);
        return ResponseEntity.created(URI.create("/api/assignments/" + created.getId()))
                .body(created);
    }

    //댓글 조회
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentCommentResponse> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentCommentService.getComment(id));
    }

    //댓글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<AssignmentCommentResponse> updateComment(@PathVariable Long id, @RequestBody @Valid AssignmentCommentRequest request,
                                                           @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(assignmentCommentService.updateComment(id, request, userId));
    }

    //댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<AssignmentCommentResponse> deleteComment(@PathVariable Long id, @RequestBody @Valid AssignmentCommentRequest request,
                                                                   @AuthenticationPrincipal Long userId){
        assignmentCommentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
}
