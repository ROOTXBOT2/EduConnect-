package com.BugJava.EduConnect.qnaboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.qnaboard.dto.CommentCreateRequest;
import com.BugJava.EduConnect.qnaboard.dto.CommentResponse;
import com.BugJava.EduConnect.qnaboard.dto.CommentUpdateRequest;
import com.BugJava.EduConnect.qnaboard.entity.Answer;
import com.BugJava.EduConnect.qnaboard.entity.Comment;
import com.BugJava.EduConnect.qnaboard.exception.AccessDeniedException;
import com.BugJava.EduConnect.qnaboard.exception.AnswerNotFoundException;
import com.BugJava.EduConnect.qnaboard.exception.CommentNotFoundException;
import com.BugJava.EduConnect.qnaboard.repository.AnswerRepository;
import com.BugJava.EduConnect.qnaboard.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(Long answerId, Long userId, CommentCreateRequest req) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("로그인 사용자 없음"));

        Answer answer = answerRepository.findById(answerId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new AnswerNotFoundException(answerId.toString()));

        Comment comment = Comment.builder()
                .content(req.getContent())
                .user(user)
                .answer(answer)
                .isDeleted(false)
                .build();

        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long answerId) {
        if (!answerRepository.existsById(answerId)) {
            throw new AnswerNotFoundException(answerId.toString());
        }

        return commentRepository.findByAnswerIdAndIsDeletedFalse(answerId).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateComment(Long commentId, Long userId, CommentUpdateRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new CommentNotFoundException(commentId.toString()));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 댓글만 수정할 수 있습니다.");
        }

        comment.change(req.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new CommentNotFoundException(commentId.toString()));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 댓글만 삭제할 수 있습니다.");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }
}
