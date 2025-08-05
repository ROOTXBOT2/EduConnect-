package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.exception.CommentNotFoundException;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbCommentRepository;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import lombok.RequiredArgsConstructor;
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

    public FbCommentResponse getComment(Long id) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));
        return FbCommentResponse.from(comment);
    }

    @Transactional
    public FbCommentResponse createComment(Long postId, FbCommentRequest request, Long userId) {
        FbPost post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

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
    public FbCommentResponse updateComment(Long id, FbCommentRequest request) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(comment.getUser().getId());

        comment.setContent(request.getContent());

        return FbCommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(comment.getUser().getId());

        commentRepository.delete(comment);
    }
}