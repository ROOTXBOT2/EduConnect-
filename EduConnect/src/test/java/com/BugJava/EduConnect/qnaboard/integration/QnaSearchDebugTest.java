package com.BugJava.EduConnect.qnaboard.integration;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.service.JwtTokenProvider;
import com.BugJava.EduConnect.qnaboard.dto.QuestionSearchRequest;
import com.BugJava.EduConnect.qnaboard.entity.Question;
import com.BugJava.EduConnect.qnaboard.repository.QuestionRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QnA Search 디버그 테스트
 * 
 * @author rua
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QnaSearchDebugTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private Users student;
    private String studentToken;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        userRepository.deleteAll();

        student = userRepository.save(Users.builder()
                .email("student@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("테스트학생")
                .role(Role.STUDENT)
                .track(Track.BACKEND)
                .isDeleted(false)
                .build());

        // JWT 토큰 생성
        studentToken = jwtTokenProvider.createAccessToken(student.getId(), student.getRole());

        // 테스트 데이터 생성
        questionRepository.save(Question.builder()
                .user(student)
                .title("Spring Boot 기초 질문")
                .content("Spring Boot 설정에 대해")
                .track(Track.BACKEND)
                .isDeleted(false)
                .build());
        
        questionRepository.save(Question.builder()
                .user(student)
                .title("React 질문입니다")
                .content("React Hook에 대해")
                .track(Track.FRONTEND)
                .isDeleted(false)
                .build());
    }

    @Test
    @DisplayName("검색 디버그 테스트")
    void debugSearch() throws Exception {
        // given
        QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
                .keyword("Spring")
                .build();

        // when & then
        MvcResult result = mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("Search Response: " + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("트랙 검색 디버그 테스트")
    void debugTrackSearch() throws Exception {
        // given
        QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
                .track(Track.BACKEND)
                .build();

        // when & then
        MvcResult result = mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("Track Search Response: " + result.getResponse().getContentAsString());
    }
}