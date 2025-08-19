package com.BugJava.EduConnect.integration.assignment;

import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.dto.AssignmentCommentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentCommentResponse;
import com.BugJava.EduConnect.assignment.service.AssignmentService;
import com.BugJava.EduConnect.assignment.service.AssignmentCommentService;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller/Service/DTO/Path 그대로 사용:
 * - POST   /api/assignments/{assignmentId}/comments
 * - PATCH  /api/assignments/{assignmentId}/comments/{id}
 * - DELETE /api/assignments/{assignmentId}/comments/{id}
 * 응답은 래핑 없이 DTO 본문이므로 $.content 등으로 검증.
 */
@DisplayName("AssignmentCommentController 통합 테스트")
class AssignmentCommentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AssignmentCommentService assignmentCommentService;

    private String accessToken;
    private String anotherUserAccessToken;
    private Users testUser;
    private Users anotherUser;

    private AssignmentResponse testAssignment;
    private AssignmentCommentResponse testRootComment;
    private AssignmentCommentResponse anotherUserRootComment;

    @BeforeEach
    void setUp() {
        // 과제 만들 수 있도록 INSTRUCTOR 권한
        UserAuthInfo testUserAuth = registerAndLoginUser("testuser@example.com", "password", "testuser", Role.INSTRUCTOR);
        accessToken = testUserAuth.accessToken;
        testUser = testUserAuth.user;

        UserAuthInfo anotherUserAuth = registerAndLoginUser("anotheruser@example.com", "password", "anotheruser");
        anotherUserAccessToken = anotherUserAuth.accessToken;
        anotherUser = anotherUserAuth.user;

        // 과제 시드
        AssignmentRequest assignmentRequest = new AssignmentRequest("Test Assignment for Comment", "Desc");
        testAssignment = assignmentService.createAssignment(assignmentRequest, testUser.getId());

        // 댓글 시드 (서비스로 생성)
        AssignmentCommentRequest rootReq = new AssignmentCommentRequest("Root by TestUser", null);
        testRootComment = assignmentCommentService.createComment(testAssignment.getId(), rootReq, testUser.getId());

        AssignmentCommentRequest otherRootReq = new AssignmentCommentRequest("Root by AnotherUser", null);
        anotherUserRootComment = assignmentCommentService.createComment(testAssignment.getId(), otherRootReq, anotherUser.getId());
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {
        @Test
        @DisplayName("성공 - 루트 댓글")
        void createComment_success_root() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("새 루트 댓글", null);

            mockMvc.perform(post("/api/assignments/{assignmentId}/comments", testAssignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.content").value("새 루트 댓글"))
                    .andDo(print());
        }

        @Test
        @DisplayName("성공 - 대댓글(parentId 지정)")
        void createComment_success_child() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("새 대댓글", testRootComment.getId());

            mockMvc.perform(post("/api/assignments/{assignmentId}/comments", testAssignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.parentId").value(testRootComment.getId()))
                    .andExpect(jsonPath("$.content").value("새 대댓글"));
        }

        @Test
        @DisplayName("실패 - 미인증(401)")
        void createComment_unauthorized() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("인증 없이 생성", null);

            mockMvc.perform(post("/api/assignments/{assignmentId}/comments", testAssignment.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 검증오류(400) content 누락")
        void createComment_validationFailure() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("", null);

            mockMvc.perform(post("/api/assignments/{assignmentId}/comments", testAssignment.getId())
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
        @DisplayName("성공 - 작성자 본인")
        void updateComment_success_owner() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("수정된 내용", null);

            mockMvc.perform(patch("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), testRootComment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("수정된 내용"));
        }

        @Test
        @DisplayName("실패 - 권한 없음(403)")
        void updateComment_forbidden() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("남의 댓글 수정 시도", null);

            mockMvc.perform(patch("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), testRootComment.getId())
                            .header("Authorization", "Bearer " + anotherUserAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 없음(404)")
        void updateComment_notFound() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("없는 댓글 수정", null);

            mockMvc.perform(patch("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), 999_999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 검증오류(400) content 누락")
        void updateComment_validationFailure() throws Exception {
            AssignmentCommentRequest request = new AssignmentCommentRequest("", null);

            mockMvc.perform(patch("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), testRootComment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {
        @Test
        @DisplayName("성공 - 작성자 본인")
        void deleteComment_success_owner() throws Exception {
            mockMvc.perform(delete("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), anotherUserRootComment.getId())
                            .header("Authorization", "Bearer " + anotherUserAccessToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 권한 없음(403)")
        void deleteComment_forbidden() throws Exception {
            mockMvc.perform(delete("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), anotherUserRootComment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 없음(404)")
        void deleteComment_notFound() throws Exception {
            mockMvc.perform(delete("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), 999_999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 미인증(401)")
        void deleteComment_unauthorized() throws Exception {
            mockMvc.perform(delete("/api/assignments/{assignmentId}/comments/{id}", testAssignment.getId(), testRootComment.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
