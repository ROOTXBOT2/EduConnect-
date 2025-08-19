package com.BugJava.EduConnect.qnaboard.integration;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.service.JwtTokenProvider;
import com.BugJava.EduConnect.qnaboard.dto.QuestionSearchRequest;
import com.BugJava.EduConnect.qnaboard.entity.Question;
import com.BugJava.EduConnect.qnaboard.repository.QuestionRepository;
import com.BugJava.EduConnect.qnaboard.utils.QnaTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QnA 검색 성능 및 엣지 케이스 테스트
 * 
 * @author rua
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QnaSearchPerformanceTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private Users testUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        userRepository.deleteAll();

        Users userToSave = QnaTestDataBuilder.createSampleStudent(
                "test@example.com", "테스트사용자", Track.BACKEND);
        userToSave = Users.builder()
                .email(userToSave.getEmail())
                .password(passwordEncoder.encode("password123"))
                .name(userToSave.getName())
                .role(userToSave.getRole())
                .track(userToSave.getTrack())
                .isDeleted(false)
                .build();
        testUser = userRepository.save(userToSave);

        // JWT 토큰 생성
        userToken = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getRole());
    }

    @Test
    @DisplayName("대용량 데이터에서 검색 성능 테스트")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void searchPerformanceWithLargeData() throws Exception {
        // given - 1000개의 질문 생성
        createLargeTestData(1000);

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword("질문")
                .build();

        // when & then - 5초 내에 응답해야 함
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("페이징 성능 테스트 - 마지막 페이지 조회")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void pagingPerformanceLastPage() throws Exception {
        // given
        createLargeTestData(500);

        // when & then - 마지막 페이지 조회도 3초 내에 응답
        mockMvc.perform(get("/api/qna/questions")
                .header("Authorization", "Bearer " + userToken)
                .param("page", "24") // 500개 데이터의 마지막 페이지 (page size 20)
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("특수 문자 키워드 검색 테스트")
    void searchWithSpecialCharacters() throws Exception {
        // given - 특수 문자가 포함된 질문 생성
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "C++ 포인터 질문", "C++ 포인터(*) 관련 질문입니다", Track.BACKEND));
        
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "SQL 질문 (SELECT * FROM)", "SQL 쿼리 관련", Track.BACKEND));

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword("C++")
                .build();

        // when & then
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("C++ 포인터 질문"));
    }

    @Test
    @DisplayName("한글과 영문 혼합 키워드 검색")
    void searchWithMixedLanguageKeyword() throws Exception {
        // given
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "Spring Boot 설정", "Spring Boot configuration", Track.BACKEND));
        
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "React Hook 사용법", "React Hook usage", Track.FRONTEND));

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword("Boot")
                .build();

        // when & then
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot 설정"));
    }

    @Test
    @DisplayName("매우 긴 키워드 검색 테스트")
    void searchWithVeryLongKeyword() throws Exception {
        // given - 매우 긴 키워드 (100자)
        String longKeyword = "a".repeat(100);
        
        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword(longKeyword)
                .build();

        // when & then - 에러 없이 처리되어야 함
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    @DisplayName("빈 키워드 검색 - 전체 목록 반환")
    void searchWithEmptyKeyword() throws Exception {
        // given
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "질문1", "내용1", Track.BACKEND));
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "질문2", "내용2", Track.FRONTEND));

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword("")
                .build();

        // when & then
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @DisplayName("null 키워드 검색 - 전체 목록 반환")
    void searchWithNullKeyword() throws Exception {
        // given
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "질문1", "내용1", Track.BACKEND));

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword(null)
                .build();

        // when & then
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("공백만 있는 키워드 검색")
    void searchWithWhitespaceOnlyKeyword() throws Exception {
        // given
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "질문1", "내용1", Track.BACKEND));

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword("   ")
                .build();

        // when & then
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("부분 단어 검색 테스트")
    void searchWithPartialWord() throws Exception {
        // given
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "JavaScript 기초", "자바스크립트 기초 내용", Track.FRONTEND));
        
        questionRepository.save(QnaTestDataBuilder.createSampleQuestion(
                testUser, "Java 기초", "자바 기초 내용", Track.BACKEND));

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword("Java")
                .build();

        // when & then - JavaScript도 Java를 포함하므로 2개 모두 검색
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @DisplayName("복합 조건 검색 성능 테스트")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void complexSearchPerformance() throws Exception {
        // given - 여러 트랙의 데이터 생성
        createMixedTrackData(200);

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .track(Track.BACKEND)
                .keyword("Spring")
                .build();

        // when & then - 복합 조건도 2초 내에 응답
        mockMvc.perform(post("/api/qna/questions/search")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("동시 검색 요청 처리 테스트")
    void concurrentSearchRequests() throws Exception {
        // given
        createLargeTestData(100);

        QuestionSearchRequest searchRequest = QnaTestDataBuilder.aQuestionSearchRequest()
                .keyword("질문")
                .build();

        // when & then - 여러 동시 요청 처리
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/qna/questions/search")
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk());
        }
    }

    private void createLargeTestData(int count) {
        List<Question> questions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Track track = Track.values()[i % 4]; // 트랙 순환
            questions.add(QnaTestDataBuilder.createSampleQuestion(
                    testUser, 
                    String.format("테스트 질문 %d", i),
                    String.format("테스트 내용 %d", i),
                    track));
        }
        
        questionRepository.saveAll(questions);
    }

    private void createMixedTrackData(int count) {
        List<Question> questions = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Track track = Track.values()[i % 4];
            String titleKeyword = (track == Track.BACKEND) ? "Spring" : "React";
            
            questions.add(QnaTestDataBuilder.createSampleQuestion(
                    testUser,
                    String.format("%s 질문 %d", titleKeyword, i),
                    String.format("%s 관련 내용 %d", titleKeyword, i),
                    track));
        }
        
        questionRepository.saveAll(questions);
    }
}