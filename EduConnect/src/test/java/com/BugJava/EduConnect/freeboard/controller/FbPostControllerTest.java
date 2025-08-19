package com.BugJava.EduConnect.freeboard.controller;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
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
public class FbPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FbPostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Users testUser;

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
        IntStream.rangeClosed(1, 20).forEach(i -> {
            FbPost post = FbPost.builder()
                    .title("제목" + i)
                    .content("내용" + i)
                    .user(testUser)
                    .build();
            post.setCreatedAt(LocalDateTime.now().minusDays(20 - i)); // setCreatedAt 사용
            postRepository.save(post);
        });
    }

    @Test
    @DisplayName("게시글 목록 조회 - 기본 페이징 (최신순 10개)")
    void getAllPosts_defaultPaging() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(10)) // .data.content로 변경
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(20))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.last").value(false))
                .andExpect(jsonPath("$.data.content[0].title").value("제목20")) // 최신순
                .andExpect(jsonPath("$.data.content[9].title").value("제목11"));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 커스텀 페이징 (2페이지, 5개씩, 오래된순)")
    void getAllPosts_customPagingAndSort() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "createdAt,asc") // 오래된 순
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(5)) // .data.content로 변경
                .andExpect(jsonPath("$.data.pageNumber").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(20))
                .andExpect(jsonPath("$.data.totalPages").value(4))
                .andExpect(jsonPath("$.data.content[0].title").value("제목6")) // 오래된 순
                .andExpect(jsonPath("$.data.content[4].title").value("제목10"));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 제목 검색")
    void getAllPosts_searchByTitle() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("searchType", "title")
                        .param("searchKeyword", "제목15")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1)) // .data.content로 변경
                .andExpect(jsonPath("$.data.content[0].title").value("제목15"));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 내용 검색")
    void getAllPosts_searchByContent() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("searchType", "content")
                        .param("searchKeyword", "내용5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1)) // .data.content로 변경
                .andExpect(jsonPath("$.data.content[0].title").value("제목5"));
    }

    @Test
    @DisplayName("게시글 목록 조회 - 작성자 검색")
    void getAllPosts_searchByAuthor() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("searchType", "author")
                        .param("searchKeyword", "테스트유저")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(10)); // .data.content로 변경
    }

    @Test
    @DisplayName("게시글 목록 조회 - 검색 결과 없음")
    void getAllPosts_noSearchResult() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("searchType", "title")
                        .param("searchKeyword", "없는제목")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(0)); // .data.content로 변경
    }
}