package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.FbPostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FbPostService {

    private final FbPostRepository postRepository;

    /** 게시글 전체 조회 */
    public List<FbPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 게시글 단건 조회 (댓글 포함) */
    public FbPostResponse getPost(Long id) {
        FbPost post = postRepository.findById(id)
                .orElseThrow(() -> new FbPostNotFoundException(id));

        List<FbCommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> FbCommentResponse.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .author(comment.getAuthor())
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .postId(post.getId())
                        .build())
                .collect(Collectors.toList());

        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(commentResponses)
                .build();
    }

    /** 게시글 생성 */
    @Transactional
    public FbPostResponse createPost(FbPostRequest request) {
        FbPost post = FbPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .build();

        return toResponse(postRepository.save(post));
    }

    /** 게시글 수정 */
    @Transactional
    public FbPostResponse updatePost(Long id, FbPostRequest request) {
        FbPost post = postRepository.findById(id)
                .orElseThrow(() -> new FbPostNotFoundException(id));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());

        return toResponse(post); // JPA Dirty Checking
    }

    /** 게시글 삭제 */
    @Transactional
    public void deletePost(Long id) {
        FbPost post = postRepository.findById(id)
                .orElseThrow(() -> new FbPostNotFoundException(id));
        postRepository.delete(post);
    }

    /** Entity → DTO 변환 메서드 */
    private FbPostResponse toResponse(FbPost post) {
        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

