package com.BugJava.EduConnect.integration;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.ChatSessionResponse;
import com.BugJava.EduConnect.chat.dto.RoomRequest;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import com.BugJava.EduConnect.chat.service.ChatSessionService;
import com.BugJava.EduConnect.chat.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.BugJava.EduConnect.util.TestUtils.createChatSession;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ChatSessionController 통합 테스트")
class ChatSessionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    private String instructorToken;
    private Users instructorUser;
    private String studentToken;
    private Users studentUser;
    private RoomResponse testRoomResponse;
    private Room testRoomEntity;

    @BeforeEach
    void setUp() {
        // 강사 유저
        UserAuthInfo instructorAuth = registerAndLoginUser("session_instructor@example.com", "password", "세션강사", Role.INSTRUCTOR);
        instructorToken = instructorAuth.accessToken;
        instructorUser = instructorAuth.user;

        // 학생 유저
        UserAuthInfo studentAuth = registerAndLoginUser("session_student@example.com", "password", "세션학생", Role.STUDENT);
        studentToken = studentAuth.accessToken;
        studentUser = studentAuth.user;

        // 테스트용 강의실 생성
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setTitle("세션 테스트 강의실");
        testRoomResponse = roomService.createRoom(instructorUser.getId(), roomRequest);
        testRoomEntity = roomService.findRoomEntityByCode(testRoomResponse.getCode());
    }

    @Nested
    @DisplayName("활성 세션 조회 (getActiveSession)")
    class GetTodaySessionTests {

        @Test
        @DisplayName("성공 - 강사가 세션이 있을 때 기존 세션 반환")
        void success_instructorReturnsExistingSession() throws Exception {
            // Given
            chatSessionService.startNewSession(testRoomResponse.getCode(), instructorUser.getId()); // 세션 미리 생성

            // When & Then
            mockMvc.perform(get("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/active")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("활성화된 채팅 세션 정보입니다."))
                    .andExpect(jsonPath("$.data.active").value(true));
        }

        @Test
        @DisplayName("성공 - 세션이 없을 때 204 No Content 반환")
        void success_noSessionReturns204() throws Exception {
            mockMvc.perform(get("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/active")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("성공 - 학생이 세션이 있을 때 기존 세션 반환")
        void success_studentReturnsExistingSession() throws Exception {
            // Given
            chatSessionService.startNewSession(testRoomResponse.getCode(), instructorUser.getId()); // 강사가 세션 미리 생성

            // When & Then
            mockMvc.perform(get("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/active")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("활성화된 채팅 세션 정보입니다."))
                    .andExpect(jsonPath("$.data.active").value(true));
        }

        @Test
        @DisplayName("실패 - 학생이 세션이 없을 때 예외 발생 (Forbidden)")
        void failure_studentNoSessionThrowsException() throws Exception {
            // 세션이 없는 상태에서 학생이 접근
            mockMvc.perform(get("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/active")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden()); // SessionNotStartedException -> 403 Forbidden
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void failure_unauthorized() throws Exception {
            mockMvc.perform(get("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/active"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실 (Not Found)")
        void failure_roomNotFound() throws Exception {
            mockMvc.perform(get("/api/chat/rooms/NONEXISTENT_CODE/sessions/active")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("종료된 세션 기록 목록 조회 (/api/chat/rooms/{roomCode}/sessions/archive)")
    class GetArchivedSessions {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // Given: 종료된 세션 생성
            ChatSessionResponse session1Response = chatSessionService.startNewSession(testRoomResponse.getCode(), instructorUser.getId());
            ChatSession session1 = chatSessionRepository.findById(session1Response.getSessionId()).orElseThrow();
            session1.close();
            chatSessionRepository.save(session1);

            // 다른 날짜의 세션 생성 및 종료
            RoomRequest anotherRoomRequest = new RoomRequest();
            anotherRoomRequest.setTitle("다른 강의실");
            RoomResponse anotherRoomResponse = roomService.createRoom(instructorUser.getId(), anotherRoomRequest);
            Room anotherRoomEntity = roomService.findRoomEntityByCode(anotherRoomResponse.getCode());

            ChatSession session2 = new ChatSession(anotherRoomEntity, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 수업 기록");
            session2.close();
            chatSessionRepository.save(session2);

            mockMvc.perform(get("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/archive")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("종료된 채팅 세션 기록 목록 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1)); // testRoomResponse에 대한 세션만 조회
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void failure_unauthorized() throws Exception {
            mockMvc.perform(get("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/archive"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실 (Not Found)")
        void failure_roomNotFound() throws Exception {
            mockMvc.perform(get("/api/chat/rooms/NONEXISTENT_CODE/sessions/archive")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("세션 종료 (/api/chat/rooms/{roomCode}/sessions/{sessionId}/close)")
    class CloseSession {

        private Long sessionIdToClose;

        @BeforeEach
        void setUpSession() {
            // Given: 오늘 세션 생성
            ChatSessionResponse sessionResponse = chatSessionService.startNewSession(testRoomResponse.getCode(), instructorUser.getId());
            sessionIdToClose = sessionResponse.getSessionId();
        }

        @Test
        @DisplayName("성공 - 강사가 세션 종료")
        void success_instructorClosesSession() throws Exception {
            mockMvc.perform(post("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/" + sessionIdToClose + "/close")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("채팅 세션이 성공적으로 종료되었습니다."));

            // 종료 확인
            Optional<ChatSession> closedSession = chatSessionRepository.findById(sessionIdToClose);
            assertThat(closedSession.isPresent()).isTrue();
            assertThat(closedSession.get().getStatus()).isEqualTo(SessionStatus.CLOSED);
        }

        @Test
        @DisplayName("실패 - 학생이 세션 종료 (Forbidden)")
        void failure_studentClosesSession() throws Exception {
            mockMvc.perform(post("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/" + sessionIdToClose + "/close")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void failure_unauthorized() throws Exception {
            mockMvc.perform(post("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/" + sessionIdToClose + "/close"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 세션 (Not Found)")
        void failure_sessionNotFound() throws Exception {
            mockMvc.perform(post("/api/chat/rooms/" + testRoomResponse.getCode() + "/sessions/99999/close")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }
}
