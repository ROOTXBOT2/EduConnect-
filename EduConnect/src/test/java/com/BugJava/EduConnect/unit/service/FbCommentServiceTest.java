package com.BugJava.EduConnect.unit.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.util.AuthorizationUtil;
import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.exception.CommentNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbCommentRepository;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import com.BugJava.EduConnect.freeboard.service.FbCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FbCommentServiceTest {

    @Mock
    private FbCommentRepository fbCommentRepository;

    @Mock
    private FbPostRepository fbPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @InjectMocks
    private FbCommentService fbCommentService;

    private Users testUser;
    private FbPost testPost;
    private Long testUserId = 1L;
    private Long testPostId = 1L;

    @BeforeEach
    void setUp() {
        testUser = Users.builder()
                .id(testUserId)
                .name("testUser")
                .build();

        testPost = FbPost.builder()
                .id(testPostId)
                .title("test post")
                .content("test content")
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("댓글 생성")
    void createComment() {
        // given
        FbCommentRequest request = new FbCommentRequest("new comment");
        FbComment comment = FbComment.builder().content("new comment").user(testUser).post(testPost).build();

        when(fbPostRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(fbCommentRepository.save(any(FbComment.class))).thenReturn(comment);

        // when
        FbCommentResponse createdComment = fbCommentService.createComment(testPostId, request, testUserId);

        // then
        assertThat(createdComment.getContent()).isEqualTo("new comment");
    }

    @Test
    @DisplayName("댓글 수정")
    void updateComment() {
        // given
        Long commentId = 1L;
        FbCommentRequest request = new FbCommentRequest("updated comment");
        FbComment comment = FbComment.builder().id(commentId).content("original comment").user(testUser).post(testPost).build();

        when(fbCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        fbCommentService.updateComment(commentId, request, testUserId);

        // then
        verify(authorizationUtil).checkOwnerOrAdmin(testUser.getId());
        assertThat(comment.getContent()).isEqualTo("updated comment");
    }

    @Test
    @DisplayName("댓글 수정 권한 없음")
    void updateComment_accessDenied() {
        // given
        Long commentId = 1L;
        FbCommentRequest request = new FbCommentRequest("updated comment");
        FbComment comment = FbComment.builder().id(commentId).content("original comment").user(testUser).post(testPost).build();

        when(fbCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(testUser.getId());

        // when & then
        assertThatThrownBy(() -> fbCommentService.updateComment(commentId, request, testUserId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외 발생")
    void updateComment_notFound() {
        // given
        Long commentId = 1L;
        FbCommentRequest request = new FbCommentRequest("updated comment");

        when(fbCommentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fbCommentService.updateComment(commentId, request, testUserId))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() {
        // given
        Long commentId = 1L;
        FbComment comment = FbComment.builder().id(commentId).content("comment").user(testUser).post(testPost).build();

        when(fbCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        fbCommentService.deleteComment(commentId, testUserId);

        // then
        verify(authorizationUtil).checkOwnerOrAdmin(testUser.getId());
        verify(fbCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("댓글 삭제 권한 없음")
    void deleteComment_accessDenied() {
        // given
        Long commentId = 1L;
        FbComment comment = FbComment.builder().id(commentId).content("comment").user(testUser).post(testPost).build();

        when(fbCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(testUser.getId());

        // when & then
        assertThatThrownBy(() -> fbCommentService.deleteComment(commentId, testUserId))
                .isInstanceOf(AccessDeniedException.class);
    }
}