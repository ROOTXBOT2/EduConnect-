package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.exception.FbCommentNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbCommentRepository;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import lombok.RequiredArgsConstructor;
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


    /** 댓글 단건 조회 */
    public FbCommentResponse getComment(Long id) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FbCommentNotFoundException(id));
        return toResponse(comment);
    }

    /** 댓글 생성 */
    @Transactional
    public FbCommentResponse createComment(Long postId, FbCommentRequest request) {
        FbPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글 ID입니다."));

        FbComment comment = FbComment.builder()
                .content(request.getContent())
                .author(request.getAuthor())
                .post(post)
                .build();

        return toResponse(commentRepository.save(comment));
    }

    /** 댓글 수정 */
    @Transactional
    public FbCommentResponse updateComment(Long id, FbCommentRequest request) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FbCommentNotFoundException(id));

        comment.setContent(request.getContent());
        comment.setAuthor(request.getAuthor());

        return toResponse(comment);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(Long id) {
        FbComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FbCommentNotFoundException(id));
        commentRepository.delete(comment);
    }

    private FbCommentResponse toResponse(FbComment comment) {
        return FbCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .postId(comment.getPost().getId())
                .build();
    }
}

