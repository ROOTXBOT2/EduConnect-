package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.exception.FbCommentNotFoundException;
import com.BugJava.EduConnect.freeboard.exception.FbPostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbCommentRepository;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import com.BugJava.EduConnect.common.dto.PageResponse; // PageResponse 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Page 임포트
import org.springframework.data.domain.Pageable; // Pageable 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FbCommentService {

    private final FbCommentRepository commentRepository;
    private final FbPostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthorizationUtil authorizationUtil;

    public PageResponse<FbCommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        Page<FbComment> commentsPage = commentRepository.findByPostId(postId, pageable);
        return new PageResponse<>(commentsPage.map(FbCommentResponse::from));
    }

    public FbCommentResponse getComment(Long id) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FbCommentNotFoundException("댓글을 찾을 수 없습니다."));
        return FbCommentResponse.from(comment);
    }

    @Transactional
    public FbCommentResponse createComment(Long postId, FbCommentRequest request, Long userId) {
        FbPost post = postRepository.findById(postId)
                .orElseThrow(() -> new FbPostNotFoundException("게시글을 찾을 수 없습니다."));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        FbComment comment = FbComment.builder()
                .content(request.getContent())
                .user(user)
                .post(post)
                .build();

        return FbCommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public FbCommentResponse updateComment(Long postId, Long id, FbCommentRequest request) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FbCommentNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글에 속하지 않는 댓글입니다.");
        }

        authorizationUtil.checkOwnerOrAdmin(comment.getUser().getId());

        comment.setContent(request.getContent());
        commentRepository.save(comment); // 명시적 save 호출 추가

        return FbCommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long id) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FbCommentNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글에 속하지 않는 댓글입니다.");
        }

        authorizationUtil.checkOwnerOrAdmin(comment.getUser().getId());

        commentRepository.delete(comment);
    }
}