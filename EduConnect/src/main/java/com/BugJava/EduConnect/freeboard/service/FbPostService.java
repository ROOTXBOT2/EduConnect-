package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
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
    private final UserRepository userRepository;
    private final AuthorizationUtil authorizationUtil;

    public List<FbPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FbPostResponse getPost(Long id) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        List<FbCommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> FbCommentResponse.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .authorName(comment.getUser().getName())
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .postId(post.getId())
                        .build())
                .collect(Collectors.toList());

        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getUser().getName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(commentResponses)
                .build();
    }

    @Transactional
    public FbPostResponse createPost(FbPostRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다."));

        FbPost post = FbPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return toResponse(postRepository.save(post));
    }

    @Transactional
    public FbPostResponse updatePost(Long id, FbPostRequest request, Long userId) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(post.getUser().getId(), userId);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        return toResponse(post);
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(post.getUser().getId(), userId);

        postRepository.delete(post);
    }

    private FbPostResponse toResponse(FbPost post) {
        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getUser().getName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}