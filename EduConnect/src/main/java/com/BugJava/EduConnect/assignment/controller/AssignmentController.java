package com.BugJava.EduConnect.assignment.controller;
import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.service.AssignmentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                    .orElse("검증 오류입니다.");
            return ResponseEntity.badRequest().body(errorMessage);
        }
        AssignmentResponse created = assignmentService.createAssignment(request);
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
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                    .orElse("검증 오류입니다.");
            return ResponseEntity.badRequest().body(errorMessage);
        }

        return ResponseEntity.ok(assignmentService.updateAssignment(id, request));
    }

    //과제 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}