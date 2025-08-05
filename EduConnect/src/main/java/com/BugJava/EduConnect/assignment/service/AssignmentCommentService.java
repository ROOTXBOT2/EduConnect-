package com.BugJava.EduConnect.assignment.service;

import com.BugJava.EduConnect.assignment.dto.AssignmentCommentResponse;
import com.BugJava.EduConnect.assignment.exception.PostNotFoundException;
import com.BugJava.EduConnect.assignment.repository.AssignmentCommentRepository;
import com.BugJava.EduConnect.assignment.repository.AssignmentRepository;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentCommentService {

    private final AssignmentCommentRepository commentRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public AssignmentCommentResponse createComment(AssignmentCommentResquest resquest, Long userId){
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new CommentNotFoundException("사용자를 찾을 수 없습니다."));

    }
}
