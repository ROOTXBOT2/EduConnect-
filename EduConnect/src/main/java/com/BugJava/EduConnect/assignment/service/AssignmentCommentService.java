package com.BugJava.EduConnect.assignment.service;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import com.BugJava.EduConnect.assignment.domain.AssignmentComment;
import com.BugJava.EduConnect.assignment.dto.AssignmentCommentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentCommentResponse;
import com.BugJava.EduConnect.assignment.exception.PostNotFoundException;
import com.BugJava.EduConnect.assignment.repository.AssignmentCommentRepository;
import com.BugJava.EduConnect.assignment.repository.AssignmentRepository;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.assignment.exception.CommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentCommentService {

    private final AssignmentCommentRepository commentRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final AuthorizationUtil authorizationUtil;

    //댓글 등록
    @Transactional
    public AssignmentCommentResponse createComment(Long assignmentId, AssignmentCommentRequest request, Long userId){
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new CommentNotFoundException("사용자를 찾을 수 없습니다."));
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        AssignmentComment.AssignmentCommentBuilder commentBuilder = AssignmentComment.builder()
                .content(request.getContent())
                .user(user)
                .assignment(assignment);

        // 대댓글인 경우 부모 댓글 설정
        if (request.getParentId() != null) {
            AssignmentComment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));

            // 부모 댓글이 같은 게시글의 댓글인지 확인
            if (!parent.getAssignment().getId().equals(assignmentId)) {
                throw new CommentNotFoundException("다른 게시글의 댓글입니다.");
            }

            commentBuilder.parent(parent);
        }

        AssignmentComment comment = commentBuilder.build();

        AssignmentComment saved = commentRepository.save(comment);
        return AssignmentCommentResponse.from(saved);
    }

    //댓글 수정
    @Transactional
    public AssignmentCommentResponse updateComment(Long id, AssignmentCommentRequest request, Long userId){
        AssignmentComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(comment.getUser().getId());

        comment.setContent(request.getContent());

        return AssignmentCommentResponse.from(comment);
    }

    //댓글 삭제
    @Transactional
    public void deleteComment(Long id, Long userId){
        AssignmentComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(comment.getUser().getId());

        commentRepository.delete(comment);
    }
}

