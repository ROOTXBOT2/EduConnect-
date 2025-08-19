package com.BugJava.EduConnect.assignment.controller;
import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.service.AssignmentService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.net.URI;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    //과제 등록
    @PostMapping
    public ResponseEntity<?> createAssignment(@Valid @RequestBody AssignmentRequest request,
                                              @AuthenticationPrincipal Long userId){

        AssignmentResponse created = assignmentService.createAssignment(request, userId);

        URI location = URI.create("/api/assignments/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    //전체 과제 목록 조회
    @GetMapping
    public ResponseEntity<Page<AssignmentResponse>> getPagedAssignments(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AssignmentResponse> assignments = assignmentService.getPagedAssignments(pageable);
        return ResponseEntity.ok(assignments);
    }

    //과제 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignment(id));
    }

    //과제 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request));
    }

    //과제 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

}
