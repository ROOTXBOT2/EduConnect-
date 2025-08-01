package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
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
public class FbPostService {

    private final FbPostRepository postRepository;
    private final UserRepository userRepository; // UserRepository 주입

    /** 게시글 전체 조회 */
    public List<FbPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 게시글 단건 조회 (댓글 포함) */
    public FbPostResponse getPost(Long id) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));

        List<FbCommentResponse> commentResponses = post.getComments().stream()
                .map(comment -> FbCommentResponse.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .authorName(comment.getUser().getName()) // User 엔티티에서 name 가져오기
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .postId(post.getId())
                        .build())
                .collect(Collectors.toList());

        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getUser().getName()) // User 엔티티에서 name 가져오기
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(commentResponses)
                .build();
    }

    /** 게시글 생성 */
    @Transactional
    public FbPostResponse createPost(FbPostRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId)); // 적절한 예외 처리

        FbPost post = FbPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user) // User 엔티티 연결
                .build();

        return toResponse(postRepository.save(post));
    }

    /** 게시글 수정 */
    @Transactional
    public FbPostResponse updatePost(Long id, FbPostRequest request, Long userId) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("이 게시글을 수정할 권한이 없습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        // author 필드는 이제 user 엔티티를 통해 관리되므로 직접 수정하지 않음

        return toResponse(post); // JPA Dirty Checking
    }

    /** 게시글 삭제 */
    @Transactional
    public void deletePost(Long id, Long userId) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));

        if (!post.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("이 게시글을 삭제할 권한이 없습니다.");
        }
        postRepository.delete(post);
    }

    /** Entity → DTO 변환 메서드 */
    private FbPostResponse toResponse(FbPost post) {
        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getUser().getName()) // User 엔티티에서 name 가져오기
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

