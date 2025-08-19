package com.BugJava.EduConnect.assignment.service;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.repository.AssignmentRepository;
import com.BugJava.EduConnect.assignment.exception.AssignmentNotFoundException;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final AuthorizationUtil authorizationUtil;

    //과제 등록
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .user(user)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return AssignmentResponse.fromWithoutComments(saved);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getPagedAssignments(Pageable pageable) {
        return assignmentRepository.findAll(pageable)
                .map(AssignmentResponse::fromWithoutComments);
    }

//    @Hidden
//    @Deprecated
//    @Transactional(readOnly = true)
//    public List<AssignmentResponse> getAllAssignments() {
//        return assignmentRepository.findAll().stream()
//                .map(AssignmentResponse::from)
//                .collect(Collectors.toList());
//    }

    //과제 상세 조회
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(Long id) {
        Assignment assignment = assignmentRepository.findWithCommentsById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("과제를 찾을 수 없습니다."));

        return AssignmentResponse.from(assignment);
    }

    //과제 수정
    @Transactional
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findWithCommentsById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("과제를 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(assignment.getUser().getId());

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());

        return AssignmentResponse.from(assignment);
    }

    //과제 삭제
    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findWithCommentsById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("과제를 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(assignment.getUser().getId());

        assignmentRepository.delete(assignment);
    }


}
