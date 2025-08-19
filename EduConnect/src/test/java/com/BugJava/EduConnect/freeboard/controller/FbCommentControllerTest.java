package com.BugJava.EduConnect.freeboard.controller;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.repository.FbCommentRepository;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FbCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FbPostRepository postRepository;

    @Autowired
    private FbCommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Users testUser;
    private FbPost testPost;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = Users.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .name("테스트유저")
                .role(Role.STUDENT)
                .track(Track.FRONTEND)
                .isDeleted(false)
                .build();
        userRepository.save(testUser);

        // 테스트 게시글 생성
        testPost = FbPost.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .user(testUser)
                .build();
        testPost.setCreatedAt(LocalDateTime.now()); // setCreatedAt 사용
        testPost = postRepository.save(testPost);

        // 테스트 댓글 생성
        IntStream.rangeClosed(1, 20).forEach(i -> {
            FbComment comment = FbComment.builder()
                    .content("댓글" + i)
                    .user(testUser)
                    .post(testPost)
                    .build();
            comment.setCreatedAt(LocalDateTime.now().minusDays(20 - i)); // setCreatedAt 사용
            commentRepository.save(comment);
        });
    }

    @Test
    @DisplayName("댓글 목록 조회 - 기본 페이징 (등록순 10개)")
    void getCommentsByPostId_defaultPaging() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", testPost.getId())
                        .with(user(testUser.getEmail()).password("password").roles("STUDENT")) // USER 역할 제거
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(10)) // .data.content로 변경
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(20))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.last").value(false))
                .andExpect(jsonPath("$.data.content[0].content").value("댓글1")); // 등록순
    }

    @Test
    @DisplayName("댓글 목록 조회 - 커스텀 페이징 (2페이지, 5개씩, 최신순)")
    void getCommentsByPostId_customPagingAndSort() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", testPost.getId())
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "createdAt,desc") // 최신순
                        .with(user(testUser.getEmail()).password("password").roles("STUDENT")) // USER 역할 제거
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(5)) // .data.content로 변경
                .andExpect(jsonPath("$.data.pageNumber").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(20))
                .andExpect(jsonPath("$.data.totalPages").value(4))
                .andExpect(jsonPath("$.data.content[0].content").value("댓글15")); // 최신순
    }
}