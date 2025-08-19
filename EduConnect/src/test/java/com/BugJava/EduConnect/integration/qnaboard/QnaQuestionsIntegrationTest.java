package com.BugJava.EduConnect.integration.qnaboard;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.common.service.JwtTokenProvider;
import com.BugJava.EduConnect.integration.BaseIntegrationTest;
import com.BugJava.EduConnect.qnaboard.dto.QuestionCreateRequest;
import com.BugJava.EduConnect.qnaboard.dto.QuestionSearchRequest;
import com.BugJava.EduConnect.qnaboard.entity.Question;
import com.BugJava.EduConnect.qnaboard.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QnA Questions 통합 테스트
 * - CRUD 기능 테스트
 * - 검색 및 필터링 테스트
 * - 권한 관리 테스트
 * 
 * @author rua
 */
class QnaQuestionsIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;

    private Users student;
    private Users instructor;
    private Users anotherStudent;
    private String studentToken;
    private String instructorToken;
    private String anotherStudentToken;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        questionRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성
        student = createUser("student@test.com", "테스트학생", Role.STUDENT, Track.BACKEND);
        instructor = createUser("instructor@test.com", "테스트강사", Role.INSTRUCTOR, Track.TRACK_INSTRUCTOR);
        anotherStudent = createUser("student2@test.com", "다른학생", Role.STUDENT, Track.FRONTEND);

        // JWT 토큰 생성
        studentToken = jwtTokenProvider.createAccessToken(student.getId(), student.getName(), student.getRole(), student.getTrack(), student.getEmail());
        instructorToken = jwtTokenProvider.createAccessToken(instructor.getId(), instructor.getName(), instructor.getRole(), instructor.getTrack(), instructor.getEmail());
        anotherStudentToken = jwtTokenProvider.createAccessToken(anotherStudent.getId(), anotherStudent.getName(), anotherStudent.getRole(), anotherStudent.getTrack(), anotherStudent.getEmail());
    }

    private Users createUser(String email, String name, Role role, Track track) {
        return userRepository.save(Users.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .name(name)
                .role(role)
                .track(track)
                .isDeleted(false)
                .build());
    }

    @Nested
    @DisplayName("질문 CRUD 기능 테스트")
    class QuestionCrudTest {

        @Test
        @DisplayName("질문 등록 - 학생이 질문을 성공적으로 등록할 수 있다")
        void createQuestion_Student_Success() throws Exception {
            // given
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("Spring Boot 질문입니다");
            request.setContent("JPA EntityManager에 대해 궁금합니다.");
            request.setTrack(Track.BACKEND);

            try {
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
            } catch (Exception e) {
                System.err.println("Test failed with exception: " + e.getMessage());
                Throwable cause = e.getCause();
                while (cause != null) {
                    System.err.println("Caused by: " + cause.getMessage());
                    if (cause instanceof java.sql.SQLException) {
                        System.err.println("SQLState: " + ((java.sql.SQLException) cause).getSQLState());
                        System.err.println("Error Code: " + ((java.sql.SQLException) cause).getErrorCode());
                        // The SQL statement is often part of the message for SQL exceptions
                        System.err.println("SQL Exception Message: " + cause.getMessage());
                    }
                    cause = cause.getCause();
                }
                throw e; // Re-throw to ensure the test still fails
            }
        }

        @Test
        @DisplayName("질문 등록 - 강사가 질문을 성공적으로 등록할 수 있다")
        void createQuestion_Instructor_Success() throws Exception {
            // given
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("강사 질문입니다");
            request.setContent("커리큘럼 관련 문의사항입니다.");
            request.setTrack(Track.FULLSTACK);

            // when & then
            mockMvc.perform(post("/api/qna/questions")
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(questionRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("질문 등록 - 인증되지 않은 사용자는 질문을 등록할 수 없다")
        void createQuestion_Unauthorized_Fail() throws Exception {
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

            assertThat(questionRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("질문 등록 - 제목이 공백이면 실패한다")
        void createQuestion_BlankTitle_Fail() throws Exception {
            // given
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("");
            request.setContent("내용은 있습니다");
            request.setTrack(Track.BACKEND);

            // when & then
            mockMvc.perform(post("/api/qna/questions")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("질문 상세 조회 - 존재하는 질문을 조회할 수 있다")
        void getQuestionDetail_Success() throws Exception {
            // given
            Question question = createSampleQuestion(student, "테스트 질문", "테스트 내용", Track.BACKEND);

            // when & then
            mockMvc.perform(get("/api/qna/questions/{id}", question.getId())
                    .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(question.getId()))
                    .andExpect(jsonPath("$.data.title").value("테스트 질문"))
                    .andExpect(jsonPath("$.data.content").value("테스트 내용"))
                    .andExpect(jsonPath("$.data.writerName").value(student.getName()))
                    .andExpect(jsonPath("$.data.startTrack").value(student.getTrack().name()))
                    .andExpect(jsonPath("$.data.endTrack").value(Track.BACKEND.name()))
                    .andExpect(jsonPath("$.data.answerCount").value(0))
                    .andExpect(jsonPath("$.data.commentCount").value(0));
        }

        @Test
        @DisplayName("질문 상세 조회 - 존재하지 않는 질문 조회시 404 에러")
        void getQuestionDetail_NotFound() throws Exception {
            mockMvc.perform(get("/api/qna/questions/999")
                    .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("질문 삭제 - 작성자가 자신의 질문을 삭제할 수 있다")
        void deleteQuestion_Owner_Success() throws Exception {
            // given
            Question question = createSampleQuestion(student, "삭제할 질문", "내용", Track.BACKEND);

            // when & then
            mockMvc.perform(delete("/api/qna/questions/{id}", question.getId())
                    .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("QnA 삭제 완료"));

            // DB 검증 - soft delete 확인
            Optional<Question> deletedQuestion = questionRepository.findById(question.getId());
            assertThat(deletedQuestion).isPresent();
            assertThat(deletedQuestion.get().isDeleted()).isTrue();
            assertThat(deletedQuestion.get().getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("질문 삭제 - 작성자가 아닌 사용자는 삭제할 수 없다")
        void deleteQuestion_NotOwner_Fail() throws Exception {
            // given
            Question question = createSampleQuestion(student, "다른 사용자 질문", "내용", Track.BACKEND);

            // when & then
            mockMvc.perform(delete("/api/qna/questions/{id}", question.getId())
                    .header("Authorization", "Bearer " + anotherStudentToken))
                    .andExpect(status().isForbidden());

            // DB 검증 - 삭제되지 않았는지 확인
            Optional<Question> questionAfterAttempt = questionRepository.findById(question.getId());
            assertThat(questionAfterAttempt).isPresent();
            assertThat(questionAfterAttempt.get().isDeleted()).isFalse();
        }

        private Question createSampleQuestion(Users user, String title, String content, Track track) {
            return questionRepository.save(Question.builder()
                    .user(user)
                    .title(title)
                    .content(content)
                    .track(track)
                    .isDeleted(false)
                    .build());
        }
    }

    @Nested
    @DisplayName("질문 목록 조회 및 검색 테스트")
    class QuestionSearchTest {

        @BeforeEach
        void setUpQuestions() {
            // 질문 데이터만 추가로 정리 (사용자는 이미 parent에서 설정됨)
            questionRepository.deleteAll();
            
            // 다양한 테스트 데이터 생성
            createSampleQuestion(student, "Spring Boot 기초 질문", "Spring Boot 설정에 대해", Track.BACKEND);
            createSampleQuestion(student, "React 질문입니다", "React Hook에 대해", Track.FRONTEND);
            createSampleQuestion(anotherStudent, "JPA 질문", "JPA Repository에 대해", Track.BACKEND);
            createSampleQuestion(instructor, "강사 질문", "커리큘럼 관련", Track.FULLSTACK);
            
            // 삭제된 질문도 하나 생성 (검색 결과에 포함되지 않아야 함)
            Question deletedQuestion = createSampleQuestion(student, "삭제된 질문", "삭제된 내용", Track.BACKEND);
            deletedQuestion.softDelete();
            questionRepository.save(deletedQuestion);
        }

        @Test
        @DisplayName("전체 질문 목록 조회 - 페이징 동작 확인")
        void getAllQuestions_Paging_Success() throws Exception {
            mockMvc.perform(get("/api/qna/questions")
                    .header("Authorization", "Bearer " + studentToken)
                    .param("page", "0")
                    .param("size", "2")
                    .param("sort", "createdAt,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(4)) // 삭제된 것 제외
                    .andExpect(jsonPath("$.data.totalPages").value(2))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(false));
        }

        @Test
        @DisplayName("트랙별 질문 검색")
        void searchQuestionsByTrack_Success() throws Exception {
            // given
            QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
                    .track(Track.BACKEND)
                    .build();

            // when & then
            mockMvc.perform(post("/api/qna/questions/search")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2)) // BACKEND 트랙 질문만 2개
                    .andExpect(jsonPath("$.data.content[0].endTrack").value("BACKEND"))
                    .andExpect(jsonPath("$.data.content[1].endTrack").value("BACKEND"));
        }

        @Test
        @DisplayName("키워드로 질문 검색")
        void searchQuestionsByKeyword_Success() throws Exception {
            // given
            QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
                    .keyword("Spring")
                    .build();

            // when & then
            mockMvc.perform(post("/api/qna/questions/search")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot 기초 질문"));
        }

        @Test
        @DisplayName("트랙과 키워드로 복합 검색")
        void searchQuestionsByTrackAndKeyword_Success() throws Exception {
            // given
            QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
                    .track(Track.BACKEND)
                    .keyword("JPA")
                    .build();

            // when & then
            mockMvc.perform(post("/api/qna/questions/search")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("JPA 질문"))
                    .andExpect(jsonPath("$.data.content[0].endTrack").value("BACKEND"));
        }

        @Test
        @DisplayName("존재하지 않는 키워드 검색 - 빈 결과 반환")
        void searchQuestionsWithNonExistentKeyword_EmptyResult() throws Exception {
            // given
            QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
                    .keyword("존재하지않는키워드")
                    .build();

            // when & then
            mockMvc.perform(post("/api/qna/questions/search")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("대소문자 구분 없는 키워드 검색")
        void searchQuestions_CaseInsensitive() throws Exception {
            // given
            QuestionSearchRequest searchRequest = QuestionSearchRequest.builder()
                    .keyword("spring") // 소문자로 검색
                    .build();

            // when & then
            mockMvc.perform(post("/api/qna/questions/search")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot 기초 질문"));
        }

        private Question createSampleQuestion(Users user, String title, String content, Track track) {
            return questionRepository.save(Question.builder()
                    .user(user)
                    .title(title)
                    .content(content)
                    .track(track)
                    .isDeleted(false)
                    .build());
        }
    }

    @Nested
    @DisplayName("권한 및 보안 테스트")
    class AuthorizationSecurityTest {

        @Test
        @DisplayName("만료된 토큰으로 요청시 401 에러")
        void requestWithExpiredToken_Unauthorized() throws Exception {
            // given - 만료된 토큰 (실제로는 만료시키기 어려우므로 잘못된 토큰으로 대체)
            String expiredToken = "expired.token.here";

            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("토큰 테스트");
            request.setContent("내용");
            request.setTrack(Track.BACKEND);

            // when & then
            mockMvc.perform(post("/api/qna/questions")
                    .header("Authorization", "Bearer " + expiredToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("잘못된 JWT 형식으로 GET 요청시 401 에러")
        void requestWithMalformedToken_GetRequests_Unauthorized() throws Exception {
            // given
            String malformedToken = "this.is.not.a.valid.jwt.token";

            // when & then - 모든 요청이 authenticated()이므로 잘못된 토큰시 401
            mockMvc.perform(get("/api/qna/questions")
                    .header("Authorization", "Bearer " + malformedToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("잘못된 JWT 형식으로 POST 요청시 401 에러")
        void requestWithMalformedToken_PostRequests_Unauthorized() throws Exception {
            // given
            String malformedToken = "this.is.not.a.valid.jwt.token";
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("테스트 질문");
            request.setContent("내용");
            request.setTrack(Track.BACKEND);

            // when & then - POST 요청은 인증이 필요하므로 401 에러
            mockMvc.perform(post("/api/qna/questions")
                    .header("Authorization", "Bearer " + malformedToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Authorization 헤더 없이 보호된 API 호출시 401 에러")
        void requestWithoutAuthHeader_Unauthorized() throws Exception {
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("토큰 없는 요청");
            request.setContent("내용");
            request.setTrack(Track.BACKEND);

            mockMvc.perform(post("/api/qna/questions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test 
        @DisplayName("질문 수정 기능 - 유효하지 않은 요청 (400 에러)")
        void updateQuestion_InvalidRequest_BadRequest() throws Exception {
            // given
            Question question = questionRepository.save(Question.builder()
                    .user(student)
                    .title("학생 질문")
                    .content("내용")
                    .track(Track.BACKEND)
                    .isDeleted(false)
                    .build());

            // when & then - 유효하지 않은 요청으로 400 에러 발생
            mockMvc.perform(put("/api/qna/questions/{id}", question.getId())
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")) // 빈 업데이트 요청
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("삭제된 질문 조회시 404 에러")
        void getDeletedQuestion_NotFound() throws Exception {
            // given
            Question question = questionRepository.save(Question.builder()
                    .user(student)
                    .title("삭제될 질문")
                    .content("내용")
                    .track(Track.BACKEND)
                    .isDeleted(false)
                    .build());

            question.softDelete();
            questionRepository.save(question);

            // when & then
            mockMvc.perform(get("/api/qna/questions/{id}", question.getId())
                    .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("학생과 강사 모두 질문 등록 가능")
        void bothStudentAndInstructorCanCreateQuestions() throws Exception {
            // given
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle("권한 테스트 질문");
            request.setContent("내용");
            request.setTrack(Track.BACKEND);

            // when & then - 학생이 등록
            mockMvc.perform(post("/api/qna/questions")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // when & then - 강사가 등록
            mockMvc.perform(post("/api/qna/questions")
                    .header("Authorization", "Bearer " + instructorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            assertThat(questionRepository.count()).isEqualTo(2);
        }
    }
}