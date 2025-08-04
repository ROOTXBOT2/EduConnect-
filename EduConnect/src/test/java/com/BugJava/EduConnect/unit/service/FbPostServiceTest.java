package com.BugJava.EduConnect.unit.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.PostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import com.BugJava.EduConnect.freeboard.service.FbPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        testUser = Users.builder()
                .id(testUserId)
                .name("testUser")
                .build();
    }

    @Test
    @DisplayName("게시글 전체 조회")
    void getAllPosts() {
        // given
        FbPost post1 = FbPost.builder().title("title1").content("content1").user(testUser).build();
        FbPost post2 = FbPost.builder().title("title2").content("content2").user(testUser).build();
        when(fbPostRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        // when
        List<FbPostResponse> allPosts = fbPostService.getAllPosts();

        // then
        assertThat(allPosts).hasSize(2);
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void getPost() {
        // given
        Long postId = 1L;
        FbPost post = FbPost.builder().title("title").content("content").user(testUser).build();
        when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));

        // when
        FbPostResponse foundPost = fbPostService.getPost(postId);

        // then
        assertThat(foundPost.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 단건 조회 시 예외 발생")
    void getPost_notFound() {
        // given
        Long postId = 1L;
        when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fbPostService.getPost(postId))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 생성")
    void createPost() {
        // given
        FbPostRequest request = new FbPostRequest("title", "content");
        FbPost post = FbPost.builder().title("title").content("content").user(testUser).build();
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(fbPostRepository.save(any(FbPost.class))).thenReturn(post);

        // when
        FbPostResponse createdPost = fbPostService.createPost(request, testUserId);

        // then
        assertThat(createdPost.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {
        // given
        Long postId = 1L;
        FbPostRequest request = new FbPostRequest("new title", "new content");
        FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
        when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));
        doNothing().when(authorizationUtil).checkOwnerOrAdmin(anyLong(), anyLong());

        // when
        FbPostResponse updatedPost = fbPostService.updatePost(postId, request, testUserId);

        // then
        assertThat(updatedPost.getTitle()).isEqualTo("new title");
    }

    @Test
    @DisplayName("게시글 수정 권한 없음")
    void updatePost_accessDenied() {
        // given
        Long postId = 1L;
        Long anotherUserId = 2L;
        FbPostRequest request = new FbPostRequest("new title", "new content");
        FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
        when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));
        doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(anyLong(), anyLong());

        // when & then
        assertThatThrownBy(() -> fbPostService.updatePost(postId, request, anotherUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        // given
        Long postId = 1L;
        FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
        when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));
        doNothing().when(authorizationUtil).checkOwnerOrAdmin(anyLong(), anyLong());

        // when
        fbPostService.deletePost(postId, testUserId);

        // then
        verify(fbPostRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 권한 없음")
    void deletePost_accessDenied() {
        // given
        Long postId = 1L;
        Long anotherUserId = 2L;
        FbPost post = FbPost.builder().id(postId).title("title").content("content").user(testUser).build();
        when(fbPostRepository.findWithCommentsById(postId)).thenReturn(Optional.of(post));
        doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(anyLong(), anyLong());

        // when & then
        assertThatThrownBy(() -> fbPostService.deletePost(postId, anotherUserId))
                .isInstanceOf(AccessDeniedException.class);
    }
}