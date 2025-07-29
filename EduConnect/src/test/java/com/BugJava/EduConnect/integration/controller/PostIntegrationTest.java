package com.BugJava.EduConnect.integration.controller;

import com.BugJava.EduConnect.post.domain.Post;
import com.BugJava.EduConnect.post.repository.PostRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    private Long id1;
    private Long id2;
    private Long id3;

    @BeforeEach
    void setup() {
        postRepository.deleteAll();

        id1 = postRepository.save(Post.builder()
                .title("기본 제목 1")
                .content("기본 내용 1")
                .author("기본 작성자 1")
                .build()).getId();

        id2 = postRepository.save(Post.builder()
                .title("기본 제목 2")
                .content("기본 내용 2")
                .author("기본 작성자 2")
                .build()).getId();

        id3 = postRepository.save(Post.builder()
                .title("기본 제목 3")
                .content("기본 내용 3")
                .author("기본 작성자 3")
                .build()).getId();
    }

    @Test
    @DisplayName("게시글 조회 API 테스트 - 인증된 사용자")
    @WithMockUser(username = "user", roles = {"USER"})
    void getPostApiTest() throws Exception {
        mockMvc.perform(get("/api/posts/" + id2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("기본 제목 2"))
                .andExpect(jsonPath("$.author").value("기본 작성자 2"));
    }

    @Test
    @DisplayName("게시글 수정 API 테스트 - 인증 및 CSRF 토큰 포함")
    @WithMockUser(username = "user", roles = {"USER"})
    void updatePostApiTest() throws Exception {
        String json = "{\"title\":\"수정 제목\",\"content\":\"수정 내용\",\"author\":\"작성자\"}";

        mockMvc.perform(put("/api/posts/" + id1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정 제목"))
                .andExpect(jsonPath("$.content").value("수정 내용"));
    }

    @Test
    @DisplayName("게시글 삭제 API 테스트 - 인증 및 CSRF 토큰 포함")
    @WithMockUser(username = "user", roles = {"USER"})
    void deletePostApiTest() throws Exception {
        mockMvc.perform(delete("/api/posts/" + id3)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(postRepository.findById(id3)).isEmpty();
    }
}


