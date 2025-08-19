package com.BugJava.EduConnect.integration.freeboard;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.service.FbPostService;
import com.BugJava.EduConnect.integration.BaseIntegrationTest;
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

@DisplayName("FbPostController 통합 테스트")
class FbPostControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FbPostService postService;

    private String accessToken;
    private String anotherUserAccessToken;
    private Users testUser;
    private Users anotherUser;
    private FbPostResponse testPost;
    private FbPostResponse anotherUserPost;

    @BeforeEach
    void setUp() {
        UserAuthInfo testUserAuth = registerAndLoginUser("testuser@example.com", "password", "testuser");
        accessToken = testUserAuth.accessToken;
        testUser = testUserAuth.user;

        UserAuthInfo anotherUserAuth = registerAndLoginUser("anotheruser@example.com", "password", "anotheruser");
        anotherUserAccessToken = anotherUserAuth.accessToken;
        anotherUser = anotherUserAuth.user;

        FbPostRequest postRequest = new FbPostRequest("Test Post by TestUser", "Content by TestUser");
        testPost = postService.createPost(postRequest, testUser.getId());

        FbPostRequest anotherPostRequest = new FbPostRequest("Test Post by AnotherUser", "Content by AnotherUser");
        anotherUserPost = postService.createPost(anotherPostRequest, anotherUser.getId());
    }

    @Nested
    @DisplayName("게시글 생성")
    class CreatePost {
        @Test
        @DisplayName("성공")
        void createPost_success() throws Exception {
            FbPostRequest request = new FbPostRequest("새 게시글 제목", "새 게시글 내용");

            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.title").value("새 게시글 제목"))
                    .andExpect(jsonPath("$.message").value("게시글 생성 성공"))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void createPost_unauthorized() throws Exception {
            FbPostRequest request = new FbPostRequest("", "새 게시글 내용");

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 유효성 검사 (제목 누락)")
        void createPost_validationFailure() throws Exception {
            FbPostRequest request = new FbPostRequest("", "새 게시글 내용");

            mockMvc.perform(post("/api/posts")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePost {
        @Test
        @DisplayName("성공")
        void updatePost_success() throws Exception {
            FbPostRequest request = new FbPostRequest("수정된 제목", "수정된 내용");

            mockMvc.perform(put("/api/posts/{id}", testPost.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.data.content").value("수정된 내용"));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void updatePost_forbidden() throws Exception {
            FbPostRequest request = new FbPostRequest("수정된 제목", "수정된 내용");

            mockMvc.perform(put("/api/posts/{id}", anotherUserPost.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 게시글 없음")
        void updatePost_notFound() throws Exception {
            FbPostRequest request = new FbPostRequest("수정된 제목", "수정된 내용");

            mockMvc.perform(put("/api/posts/{id}", 999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("게시글 조회")
    class GetPost {
        @Test
        @DisplayName("성공 - 전체 조회")
        void getAllPosts_success() throws Exception {
            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray()) // $.data.content로 변경
                    .andExpect(jsonPath("$.data.content.length()").value(2)) // $.data.content로 변경
                    .andExpect(jsonPath("$.data.pageNumber").value(0))
                    .andExpect(jsonPath("$.data.pageSize").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.last").value(true))
                    .andExpect(jsonPath("$.data.content[0].title").value(anotherUserPost.getTitle())) // 최신순
                    .andExpect(jsonPath("$.data.content[1].title").value(testPost.getTitle())); // 최신순
        }

        @Test
        @DisplayName("성공 - 단일 조회")
        void getPost_success() throws Exception {
            mockMvc.perform(get("/api/posts/{id}", testPost.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value(testPost.getTitle()));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자")
        void getPost_unauthorized() throws Exception {
            mockMvc.perform(get("/api/posts/{id}", testPost.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 게시글 없음")
        void getPost_notFound() throws Exception {
            mockMvc.perform(get("/api/posts/{id}", 999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePost {
        @Test
        @DisplayName("성공")
        void deletePost_success() throws Exception {
            mockMvc.perform(delete("/api/posts/{id}", testPost.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void deletePost_forbidden() throws Exception {
            mockMvc.perform(delete("/api/posts/{id}", anotherUserPost.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 게시글 없음")
        void deletePost_notFound() throws Exception {
            mockMvc.perform(delete("/api/posts/{id}", 999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }
    }
}