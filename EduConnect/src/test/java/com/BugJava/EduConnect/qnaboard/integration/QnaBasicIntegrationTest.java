package com.BugJava.EduConnect.qnaboard.integration;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.service.JwtTokenProvider;
import com.BugJava.EduConnect.qnaboard.dto.QuestionCreateRequest;
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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QnA Questions 기본 통합 테스트
 * 
 * @author rua
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QnaBasicIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;

    private Users student;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        questionRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
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
    }

    @Test
    @DisplayName("질문 등록 - 학생이 질문을 성공적으로 등록할 수 있다")
    void createQuestion_Student_Success() throws Exception {
        // given
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setTitle("Spring Boot 질문입니다");
        request.setContent("JPA EntityManager에 대해 궁금합니다.");
        request.setTrack(Track.BACKEND);

        // when & then
        mockMvc.perform(post("/api/qna/questions")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("QnA 등록 완료"));

        // DB 검증
        assertThat(questionRepository.count()).isEqualTo(1);
        Question savedQuestion = questionRepository.findAll().get(0);
        assertThat(savedQuestion.getTitle()).isEqualTo("Spring Boot 질문입니다");
        assertThat(savedQuestion.getUser().getId()).isEqualTo(student.getId());
        assertThat(savedQuestion.getTrack()).isEqualTo(Track.BACKEND);
    }

    @Test
    @DisplayName("질문 목록 조회 - 인증 없이 조회 가능")
    void getAllQuestions_WithoutAuth_Success() throws Exception {
        // given - 질문 하나 생성
        questionRepository.save(Question.builder()
                .user(student)
                .title("테스트 질문")
                .content("테스트 내용")
                .track(Track.BACKEND)
                .isDeleted(false)
                .build());

        // when & then
        mockMvc.perform(get("/api/qna/questions")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("질문 상세 조회 - 인증 필요")
    void getQuestionDetail_WithAuth_Success() throws Exception {
        // given
        Question question = questionRepository.save(Question.builder()
                .user(student)
                .title("테스트 질문")
                .content("테스트 내용")
                .track(Track.BACKEND)
                .isDeleted(false)
                .build());

        // when & then
        mockMvc.perform(get("/api/qna/questions/{id}", question.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(question.getId()))
                .andExpect(jsonPath("$.data.title").value("테스트 질문"));
    }

    @Test
    @DisplayName("인증 없이 질문 등록 시도 - 401 에러")
    void createQuestion_WithoutAuth_Unauthorized() throws Exception {
        // given
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setTitle("비인증 질문");
        request.setContent("내용");
        request.setTrack(Track.BACKEND);

        // when & then
        mockMvc.perform(post("/api/qna/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        // DB 검증
        assertThat(questionRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("질문 삭제 - 작성자가 자신의 질문을 삭제할 수 있다")
    void deleteQuestion_Owner_Success() throws Exception {
        // given
        Question question = questionRepository.save(Question.builder()
                .user(student)
                .title("삭제할 질문")
                .content("내용")
                .track(Track.BACKEND)
                .isDeleted(false)
                .build());

        // when & then
        mockMvc.perform(delete("/api/qna/questions/{id}", question.getId())
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // DB 검증 - soft delete 확인
        Question deletedQuestion = questionRepository.findById(question.getId()).get();
        assertThat(deletedQuestion.isDeleted()).isTrue();
        assertThat(deletedQuestion.getDeletedAt()).isNotNull();
    }
}