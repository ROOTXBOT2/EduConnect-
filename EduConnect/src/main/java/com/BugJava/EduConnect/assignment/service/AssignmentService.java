package com.BugJava.EduConnect.assignment.service;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.repository.AssignmentRepository;
import com.BugJava.EduConnect.assignment.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    //과제 등록
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        //강사 권한 코드 추가 예정

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return AssignmentResponse.from(saved);
    }

    //전체 과제 목록 조회
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(AssignmentResponse::from)  // DTO의 from 메서드 사용
                .collect(Collectors.toList());
    }

    //과제 상세 조회
    public AssignmentResponse getAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("과제를 찾을 수 없습니다." + id));

        return AssignmentResponse.from(assignment);
    }

    //과제 수정
    @Transactional
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("과제를 찾을 수 없습니다." + id));

        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }

        return AssignmentResponse.from(assignment);
    }

    //과제 삭제
    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("과제를 찾을 수 없습니다." + id));

        assignmentRepository.delete(assignment);
    }

}
