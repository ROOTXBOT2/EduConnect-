package com.BugJava.EduConnect.assignment.service;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.repository.AssignmentRepository;
import com.BugJava.EduConnect.assignment.exception.PostNotFoundException;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    //과제 등록
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다."));
        if (user.getRole() == Role.STUDENT) {
            throw new AccessDeniedException("작성 권한이 없습니다.");
        }
        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .user(user)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return AssignmentResponse.from(saved);
    }

    //전체 과제 목록 조회
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(AssignmentResponse::from)
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
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest request, Long userId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("과제를 찾을 수 없습니다." + id));

        if (!assignment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인이 작성한 과제만 수정할 수 있습니다.");
        }

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
    public void deleteAssignment(Long id, Long userId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("과제를 찾을 수 없습니다." + id));
        if (!assignment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인이 작성한 과제만 삭제할 수 있습니다.");
        }
        assignmentRepository.delete(assignment);
    }

}
