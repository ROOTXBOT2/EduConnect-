package com.BugJava.EduConnect.assignment.controller;
import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.service.AssignmentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    //과제 등록
    @PostMapping
    public ResponseEntity<?> createAssignment(@Valid @RequestBody AssignmentRequest request,
                                              @AuthenticationPrincipal Long userId){

        AssignmentResponse created = assignmentService.createAssignment(request, userId);
        return ResponseEntity.created(URI.create("/api/assignments/" + created.getId()))
                .body(created);
    }

    //전체 과제 목록 조회
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    //과제 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignment(id));
    }

    //과제 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request,
                                              @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request, userId));
    }

    //과제 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        assignmentService.deleteAssignment(id, userId);
        return ResponseEntity.noContent().build();
    }
}