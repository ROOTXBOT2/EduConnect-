package com.BugJava.EduConnect.unit.freeboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.dto.PageResponse;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import com.BugJava.EduConnect.freeboard.service.FbPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page; // Page 임포트
import org.springframework.data.domain.PageImpl; // PageImpl 임포트
import org.springframework.data.domain.Pageable; // Pageable 임포트
import org.springframework.data.jpa.domain.Specification; // Specification 임포트
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.BugJava.EduConnect.util.TestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FbPostService 단위 테스트")
class FbPostServiceTest {

    @Mock
    private FbPostRepository fbPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @InjectMocks
    private FbPostService fbPostService;

    private Users testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(1L, "testUser", Role.STUDENT);
    }

    @Nested
    @DisplayName("게시글 조회")
    class GetPostTests {
        @Test
        @DisplayName("성공 - 전체 조회 (페이징 및 검색 포함)")
        void getAllPosts() {
            // given
            FbPost post1 = FbPost.builder().title("title1").content("content1").user(testUser).build();
            FbPost post2 = FbPost.builder().title("title2").content("content2").user(testUser).build();
            List<FbPost> posts = Arrays.asList(post1, post2);
            Page<FbPost> page = new PageImpl<>(posts, Pageable.unpaged(), posts.size());

            when(fbPostRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            // when
            // Pageable.unpaged()는 페이징 정보 없이 모든 결과를 가져옴
            // searchType과 searchKeyword는 null로 전달하여 검색 조건 없음
            PageResponse<FbPostResponse> allPosts = fbPostService.getAllPosts(Pageable.unpaged(), null, null);

            // then
            assertThat(allPosts.getContent()).hasSize(2);
            assertThat(allPosts.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 단건 조회")
        void getPost() {
            // given
            Long postId = 1L;
            FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));

            // when
            FbPostResponse foundPost = fbPostService.getPost(postId);

            // then
            assertThat(foundPost.getTitle()).isEqualTo("title");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void getPost_notFound() {
            // given
            Long postId = 1L;
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fbPostService.getPost(postId))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("게시글 생성")
    class CreatePostTests {
        @Test
        @DisplayName("성공")
        void createPost() {
            // given
            FbPostRequest request = new FbPostRequest("title", "content");
            FbPost post = FbPost.builder().title("title").content("content").user(testUser).build();
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(fbPostRepository.save(any(FbPost.class))).thenReturn(post);

            // when
            FbPostResponse createdPost = fbPostService.createPost(request, testUser.getId());

            // then
            assertThat(createdPost.getTitle()).isEqualTo("title");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void createPost_userNotFound() {
            // given
            FbPostRequest request = new FbPostRequest("title", "content");
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fbPostService.createPost(request, testUser.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePostTests {
        @Test
        @DisplayName("성공")
        void updatePost() {
            // given
            Long postId = 1L;
            FbPostRequest request = new FbPostRequest("new title", "new content");
            FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));

            // when
            fbPostService.updatePost(postId, request);

            // then
            verify(authorizationUtil).checkOwnerOrAdmin(testUser.getId());
            assertThat(post.getTitle()).isEqualTo("new title");
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void updatePost_accessDenied() {
            // given
            Long postId = 1L;
            FbPostRequest request = new FbPostRequest("new title", "new content");
            FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));
            doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(testUser.getId());

            // when & then
            assertThatThrownBy(() -> fbPostService.updatePost(postId, request))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void updatePost_postNotFound() {
            // given
            Long postId = 1L;
            FbPostRequest request = new FbPostRequest("new title", "new content");
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fbPostService.updatePost(postId, request))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePostTests {
        @Test
        @DisplayName("성공")
        void deletePost() {
            // given
            Long postId = 1L;
            FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));

            // when
            fbPostService.deletePost(postId);

            // then
            verify(authorizationUtil).checkOwnerOrAdmin(testUser.getId());
            verify(fbPostRepository).delete(post);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void deletePost_accessDenied() {
            // given
            Long postId = 1L;
            FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));
            doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(testUser.getId());

            // when & then
            assertThatThrownBy(() -> fbPostService.deletePost(postId))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void deletePost_postNotFound() {
            // given
            Long postId = 1L;
            when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fbPostService.deletePost(postId))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }
}