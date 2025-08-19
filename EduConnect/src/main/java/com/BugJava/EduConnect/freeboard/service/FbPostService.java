package com.BugJava.EduConnect.freeboard.service;

import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import com.BugJava.EduConnect.common.dto.PageResponse; // PageResponse 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Page 임포트
import org.springframework.data.domain.Pageable; // Pageable 임포트
import org.springframework.data.jpa.domain.Specification; // Specification 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // StringUtils 임포트

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FbPostService {

    private final FbPostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthorizationUtil authorizationUtil;

    public PageResponse<FbPostResponse> getAllPosts(Pageable pageable, String searchType, String searchKeyword) {
        Specification<FbPost> spec = (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchKeyword)) {
                return null; // 검색 키워드가 없으면 조건 없음
            }

            // 검색 키워드 및 컬럼 값 정규화 (소문자 변환 및 공백 제거)
            String normalizedSearchKeyword = searchKeyword.toLowerCase().replace(" ", "");

            switch (searchType) {
                case "title":
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(criteriaBuilder.function("REPLACE", String.class, root.get("title").as(String.class), criteriaBuilder.literal(" "), criteriaBuilder.literal(""))),
                            "%" + normalizedSearchKeyword + "%"
                    );
                case "content":
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(criteriaBuilder.function("REPLACE", String.class, root.get("content").as(String.class), criteriaBuilder.literal(" "), criteriaBuilder.literal(""))),
                            "%" + normalizedSearchKeyword + "%"
                    );
                case "author":
                    Join<FbPost, Users> userJoin = root.join("user", JoinType.INNER);
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(criteriaBuilder.function("REPLACE", String.class, userJoin.get("name").as(String.class), criteriaBuilder.literal(" "), criteriaBuilder.literal(""))),
                            "%" + normalizedSearchKeyword + "%"
                    );
                default:
                    return null;
            }
        };

        Page<FbPost> postsPage = postRepository.findAll(spec, pageable);
        Page<FbPostResponse> dtoPage = postsPage.map(FbPostResponse::fromWithoutComments);
        return new PageResponse<>(dtoPage);
    }

    public FbPostResponse getPost(Long id) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
        return FbPostResponse.from(post);
    }

    @Transactional
    public FbPostResponse createPost(FbPostRequest request, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        FbPost post = FbPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return FbPostResponse.fromWithoutComments(postRepository.save(post));
    }

    @Transactional
    public FbPostResponse updatePost(Long id, FbPostRequest request) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(post.getUser().getId());

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        return FbPostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id) {
        FbPost post = postRepository.findWithCommentsById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        authorizationUtil.checkOwnerOrAdmin(post.getUser().getId());

        postRepository.delete(post);
    }
}