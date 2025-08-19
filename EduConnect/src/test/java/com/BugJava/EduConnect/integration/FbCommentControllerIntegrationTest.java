package com.BugJava.EduConnect.integration;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.freeboard.dto.FbCommentRequest;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.service.FbCommentService;
import com.BugJava.EduConnect.freeboard.service.FbPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("FbCommentController 통합 테스트")
class FbCommentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FbPostService postService;

    @Autowired
    private FbCommentService commentService;

    private String accessToken;
    private String anotherUserAccessToken;
    private FbPostResponse testPost;
    private FbCommentResponse testComment;

    @BeforeEach
    void setUp() {
        UserAuthInfo testUserAuth = registerAndLoginUser("testuser@example.com", "password", "testuser");
        accessToken = testUserAuth.accessToken;
        Users testUser = testUserAuth.user;

        UserAuthInfo anotherUserAuth = registerAndLoginUser("anotheruser@example.com", "password", "anotheruser");
        anotherUserAccessToken = anotherUserAuth.accessToken;

        FbPostRequest postRequest = new FbPostRequest("Test Post", "Test Content");
        testPost = postService.createPost(postRequest, testUser.getId());

        FbCommentRequest commentRequest = new FbCommentRequest("Test Comment");
        testComment = commentService.createComment(testPost.getId(), commentRequest, testUser.getId());
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {
        @Test
        @DisplayName("성공")
        void createComment_success() throws Exception {
            FbCommentRequest request = new FbCommentRequest("새 댓글 내용");

            mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.content").value("새 댓글 내용"))
                    .andExpect(jsonPath("$.message").value("댓글 생성 성공"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void createComment_unauthorized() throws Exception {
            FbCommentRequest request = new FbCommentRequest("새 댓글 내용");

            mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 유효성 검사 (내용 누락)")
        void createComment_validationFailure() throws Exception {
            FbCommentRequest request = new FbCommentRequest("");

            mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {
        @Test
        @DisplayName("성공")
        void updateComment_success() throws Exception {
            FbCommentRequest request = new FbCommentRequest("수정된 댓글 내용");

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", testPost.getId(), testComment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").value("수정된 댓글 내용"))
                    .andExpect(jsonPath("$.message").value("댓글 수정 성공"));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void updateComment_forbidden() throws Exception {
            FbCommentRequest request = new FbCommentRequest("수정된 댓글 내용");

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", testPost.getId(), testComment.getId())
                            .header("Authorization", "Bearer " + anotherUserAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 댓글 없음")
        void updateComment_notFound() throws Exception {
            FbCommentRequest request = new FbCommentRequest("수정된 댓글 내용");

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", testPost.getId(), 999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 게시글 ID 불일치")
        void updateComment_postMismatch() throws Exception {
            FbCommentRequest request = new FbCommentRequest("수정된 댓글 내용");
            Long wrongPostId = testPost.getId() + 1; // 잘못된 게시글 ID

            mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", wrongPostId, testComment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest()) // IllegalArgumentException -> 400 Bad Request
                    .andExpect(jsonPath("$.message").value("해당 게시글에 속하지 않는 댓글입니다."));
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {
        @Test
        @DisplayName("성공")
        void deleteComment_success() throws Exception {
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", testPost.getId(), testComment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("댓글 삭제 성공"));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void deleteComment_forbidden() throws Exception {
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", testPost.getId(), testComment.getId())
                            .header("Authorization", "Bearer " + anotherUserAccessToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 댓글 없음")
        void deleteComment_notFound() throws Exception {
            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", testPost.getId(), 999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 게시글 ID 불일치")
        void deleteComment_postMismatch() throws Exception {
            Long wrongPostId = testPost.getId() + 1; // 잘못된 게시글 ID

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", wrongPostId, testComment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isBadRequest()) // IllegalArgumentException -> 400 Bad Request
                    .andExpect(jsonPath("$.message").value("해당 게시글에 속하지 않는 댓글입니다."));
        }
    }
}