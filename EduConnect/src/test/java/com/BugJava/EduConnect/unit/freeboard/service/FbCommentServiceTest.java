package com.BugJava.EduConnect.unit.freeboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
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
import com.BugJava.EduConnect.freeboard.service.FbCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static com.BugJava.EduConnect.util.TestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FbCommentService 단위 테스트")
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
    private FbComment testComment; // Added for update/delete tests

    @BeforeEach
    void setUp() {
        testUser = createUser(1L, "testUser", Role.STUDENT);
        testPost = FbPost.builder()
                .id(1L)
                .title("test post")
                .content("test content")
                .user(testUser)
                .build();
        testComment = FbComment.builder() // Initializing testComment
                .id(1L)
                .content("original comment")
                .user(testUser)
                .post(testPost)
                .build();
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateCommentTests {
        @Test
        @DisplayName("성공")
        void createComment() {
            // given
            FbCommentRequest request = new FbCommentRequest("new comment");
            FbComment comment = FbComment.builder().content("new comment").user(testUser).post(testPost).build();

            when(fbPostRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(fbCommentRepository.save(any(FbComment.class))).thenReturn(comment);

            // when
            FbCommentResponse createdComment = fbCommentService.createComment(testPost.getId(), request, testUser.getId());

            // then
            assertThat(createdComment.getContent()).isEqualTo("new comment");
        }

        @Test
        @DisplayName("실패 - 게시글 없음")
        void createComment_postNotFound() {
            // given
            FbCommentRequest request = new FbCommentRequest("new comment");
            when(fbPostRepository.findById(testPost.getId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fbCommentService.createComment(testPost.getId(), request, testUser.getId()))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void createComment_userNotFound() {
            // given
            FbCommentRequest request = new FbCommentRequest("new comment");
            when(fbPostRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fbCommentService.createComment(testPost.getId(), request, testUser.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateCommentTests {
        @Test
        @DisplayName("성공")
        void updateComment() {
            // given
            FbCommentRequest request = new FbCommentRequest("updated comment");
            when(fbCommentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
            doNothing().when(authorizationUtil).checkOwnerOrAdmin(testUser.getId());
            when(fbCommentRepository.save(any(FbComment.class))).thenReturn(testComment); // Explicit save verification

            // when
            fbCommentService.updateComment(testPost.getId(), testComment.getId(), request); // Added postId

            // then
            verify(authorizationUtil).checkOwnerOrAdmin(testUser.getId());
            assertThat(testComment.getContent()).isEqualTo("updated comment");
            verify(fbCommentRepository).save(testComment); // Verify save call
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void updateComment_accessDenied() {
            // given
            FbCommentRequest request = new FbCommentRequest("updated comment");

            when(fbCommentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
            doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(testUser.getId());

            // when & then
            assertThatThrownBy(() -> fbCommentService.updateComment(testPost.getId(), testComment.getId(), request)) // Added postId
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 댓글")
        void updateComment_notFound() {
            // given
            FbCommentRequest request = new FbCommentRequest("updated comment");

            when(fbCommentRepository.findById(testComment.getId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fbCommentService.updateComment(testPost.getId(), testComment.getId(), request)) // Added postId
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 게시글 ID 불일치")
        void updateComment_postMismatch() {
            // given
            Long wrongPostId = 99L;

            FbCommentRequest request = new FbCommentRequest("updated comment");
            when(fbCommentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));

            // when & then
            assertThatThrownBy(() -> fbCommentService.updateComment(wrongPostId, testComment.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 게시글에 속하지 않는 댓글입니다.");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentTests {
        @Test
        @DisplayName("성공")
        void deleteComment() {
            // given
            when(fbCommentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));

            // when
            fbCommentService.deleteComment(testPost.getId(), testComment.getId()); // Added postId

            // then
            verify(authorizationUtil).checkOwnerOrAdmin(testUser.getId());
            verify(fbCommentRepository).delete(testComment);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void deleteComment_accessDenied() {
            // given
            when(fbCommentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));
            doThrow(new AccessDeniedException("권한이 없습니다.")).when(authorizationUtil).checkOwnerOrAdmin(testUser.getId());

            // when & then
            assertThatThrownBy(() -> fbCommentService.deleteComment(testPost.getId(), testComment.getId())) // Added postId
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("실패 - 게시글 ID 불일치")
        void deleteComment_postMismatch() {
            // given
            Long wrongPostId = 99L;

            when(fbCommentRepository.findById(testComment.getId())).thenReturn(Optional.of(testComment));

            // when & then
            assertThatThrownBy(() -> fbCommentService.deleteComment(wrongPostId, testComment.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 게시글에 속하지 않는 댓글입니다.");
        }
    }
}