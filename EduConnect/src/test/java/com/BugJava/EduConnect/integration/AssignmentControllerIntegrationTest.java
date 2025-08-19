package com.BugJava.EduConnect.integration;

import com.BugJava.EduConnect.assignment.dto.AssignmentRequest;
import com.BugJava.EduConnect.assignment.dto.AssignmentResponse;
import com.BugJava.EduConnect.assignment.service.AssignmentService;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("AssignmentController 통합 테스트")
class AssignmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;

    private String accessToken;
    private String anotherUserAccessToken;
    private Users testUser;
    private Users anotherUser;

    private AssignmentResponse testAssignment;
    private AssignmentResponse anotherUserAssignment;

    @BeforeEach
    void setUp() {
        // 과제 생성 가능하도록 testUser는 INSTRUCTOR 권한으로 가입
        UserAuthInfo testUserAuth = registerAndLoginUser("testuser@example.com", "password", "testuser", Role.INSTRUCTOR);
        accessToken = testUserAuth.accessToken;
        testUser = testUserAuth.user;

        // anotherUser는 기본 STUDENT
        UserAuthInfo anotherUserAuth = registerAndLoginUser("anotheruser@example.com", "password", "anotheruser");
        anotherUserAccessToken = anotherUserAuth.accessToken;
        anotherUser = anotherUserAuth.user;

        // 서비스로 시드 데이터 생성 (팀원 스타일 동일)
        AssignmentRequest postRequest = new AssignmentRequest("Test Assignment by TestUser", "Desc by TestUser");
        testAssignment = assignmentService.createAssignment(postRequest, testUser.getId());

        AssignmentRequest anotherPostRequest = new AssignmentRequest("Test Assignment by AnotherUser", "Desc by AnotherUser");
        anotherUserAssignment = assignmentService.createAssignment(anotherPostRequest, testUser.getId()); // 생성자는 INSTRUCTOR여야 하므로 testUser로 생성
    }

    @Nested
    @DisplayName("과제 생성")
    class CreateAssignment {
        @Test
        @DisplayName("성공 - ROLE_INSTRUCTOR")
        void createAssignment_success() throws Exception {
            AssignmentRequest request = new AssignmentRequest("새 과제 제목", "새 과제 내용");

            mockMvc.perform(post("/api/assignments")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.title").value("새 과제 제목"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 인증 없음(401)")
        void createAssignment_unauthorized() throws Exception {
            AssignmentRequest request = new AssignmentRequest("제목", "내용");

            mockMvc.perform(post("/api/assignments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 권한 없음(403, ROLE_INSTRUCTOR 아님)")
        void createAssignment_forbidden() throws Exception {
            AssignmentRequest request = new AssignmentRequest("권한 없음 제목", "권한 없음 내용");

            mockMvc.perform(post("/api/assignments")
                            .header("Authorization", "Bearer " + anotherUserAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 검증오류(400, 제목 빈 값)")
        void createAssignment_validationFailure() throws Exception {
            AssignmentRequest request = new AssignmentRequest("", "내용");

            mockMvc.perform(post("/api/assignments")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("과제 조회")
    class GetAssignment {
        @Test
        @DisplayName("성공 - 페이지 조회(permitAll)")
        void getPagedAssignments_success() throws Exception {
            mockMvc.perform(get("/api/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        @DisplayName("성공 - 단건 조회(인증 필요)")
        void getAssignment_success() throws Exception {
            mockMvc.perform(get("/api/assignments/{id}", testAssignment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value(testAssignment.getTitle()));
        }

        @Test
        @DisplayName("실패 - 단건 조회 미인증(401)")
        void getAssignment_unauthorized() throws Exception {
            mockMvc.perform(get("/api/assignments/{id}", testAssignment.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("과제 수정")
    class UpdateAssignment {
        @Test
        @DisplayName("성공 - 소유자")
        void updateAssignment_success() throws Exception {
            AssignmentRequest request = new AssignmentRequest("수정된 제목", "수정된 내용");

            mockMvc.perform(patch("/api/assignments/{id}", testAssignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.description").value("수정된 내용"));
        }

        @Test
        @DisplayName("실패 - 권한 없음(403, 비소유자)")
        void updateAssignment_forbidden() throws Exception {
            AssignmentRequest request = new AssignmentRequest("수정된 제목", "수정된 내용");

            mockMvc.perform(patch("/api/assignments/{id}", testAssignment.getId())
                            .header("Authorization", "Bearer " + anotherUserAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 없음(404)")
        void updateAssignment_notFound() throws Exception {
            AssignmentRequest request = new AssignmentRequest("수정", "내용");

            mockMvc.perform(patch("/api/assignments/{id}", 999_999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("과제 삭제")
    class DeleteAssignment {
        @Test
        @DisplayName("성공 - 소유자")
        void deleteAssignment_success() throws Exception {
            mockMvc.perform(delete("/api/assignments/{id}", testAssignment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 권한 없음(403)")
        void deleteAssignment_forbidden() throws Exception {
            mockMvc.perform(delete("/api/assignments/{id}", anotherUserAssignment.getId())
                            .header("Authorization", "Bearer " + anotherUserAccessToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 없음(404)")
        void deleteAssignment_notFound() throws Exception {
            mockMvc.perform(delete("/api/assignments/{id}", 999_999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }
    }
}
