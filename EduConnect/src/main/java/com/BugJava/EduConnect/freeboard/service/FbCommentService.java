package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.exception.CommentNotFoundException;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbCommentRepository;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FbCommentService {

    private final FbCommentRepository commentRepository;
    private final FbPostRepository postRepository;
    private final UserRepository userRepository; // UserRepository 주입


    /** 댓글 단건 조회 */
    public FbCommentResponse getComment(Long id) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다. ID: " + id));
        return toResponse(comment);
    }

    /** 댓글 생성 */
    @Transactional
    public FbCommentResponse createComment(Long postId, FbCommentRequest request, Long userId) {
        FbPost post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new CommentNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId)); // 적절한 예외 처리

        FbComment comment = FbComment.builder()
                .content(request.getContent())
                .user(user) // User 엔티티 연결
                .post(post)
                .build();

        return toResponse(commentRepository.save(comment));
    }

    /** 댓글 수정 */
    @Transactional
    public FbCommentResponse updateComment(Long id, FbCommentRequest request, Long userId) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다. ID: " + id));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("이 댓글을 수정할 권한이 없습니다.");
        }

        comment.setContent(request.getContent());
        // author 필드는 이제 user 엔티티를 통해 관리되므로 직접 수정하지 않음

        return toResponse(comment);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(Long id, Long userId) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다. ID: " + id));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("이 댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    private FbCommentResponse toResponse(FbComment comment) {
        return FbCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getUser().getName()) // User 엔티티에서 name 가져오기
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .postId(comment.getPost().getId())
                .build();
    }
}

