package com.BugJava.EduConnect.unit.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.ChatSessionResponse;
import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.chat.exception.ChatSessionNotFoundException;
import com.BugJava.EduConnect.chat.exception.SessionNotStartedException;
import com.BugJava.EduConnect.chat.exception.UnauthorizedRoomAccessException;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import com.BugJava.EduConnect.chat.service.ChatSessionService;
import com.BugJava.EduConnect.chat.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.BugJava.EduConnect.util.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatSessionService 테스트")
class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatSessionService chatSessionService;

    private Users instructorUser;
    private Users studentUser;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        instructorUser = createUser(1L, "강사님", Role.INSTRUCTOR);
        studentUser = createUser(2L, "학생님", Role.STUDENT);
        testRoom = createRoom(1L, "ROOM123", "테스트 강의실", instructorUser);
    }

    @Nested
    @DisplayName("활성 세션 조회 (findActiveSession)")
    class FindActiveSessionTests {

        @Nested
        @DisplayName("강사인 경우")
        class Context_Instructor {
            @Test
            @DisplayName("성공 - 활성 세션이 존재할 때 기존 세션 반환")
            void success_returnExistingSession() {
                // Given
                ChatSession existingSession = createChatSession(1L, "활성 세션", testRoom);
                when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
                when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
                when(chatSessionRepository.findByRoomAndStatus(testRoom, SessionStatus.OPEN)).thenReturn(Optional.of(existingSession));

                // When
                Optional<ChatSessionResponse> responseOptional = chatSessionService.findActiveSession(testRoom.getCode(), instructorUser.getId());

                // Then
                assertThat(responseOptional).isPresent();
                ChatSessionResponse response = responseOptional.get();
                assertThat(response).isNotNull();
                assertThat(response.getSessionId()).isEqualTo(existingSession.getId());
                assertThat(response.getTitle()).isEqualTo(existingSession.getTitle());
                assertThat(response.isActive()).isTrue();
            }

            @Test
            @DisplayName("성공 - 활성 세션이 없을 때 Optional.empty() 반환")
            void success_returnEmptyOptionalWhenNoSession() {
                // Given
                when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
                when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
                when(chatSessionRepository.findByRoomAndStatus(testRoom, SessionStatus.OPEN)).thenReturn(Optional.empty());

                // When
                Optional<ChatSessionResponse> responseOptional = chatSessionService.findActiveSession(testRoom.getCode(), instructorUser.getId());

                // Then
                assertThat(responseOptional).isEmpty();
            }
        }

        @Nested
        @DisplayName("학생인 경우")
        class Context_Student {
            @Test
            @DisplayName("성공 - 활성 세션이 존재할 때 기존 세션 반환")
            void success_returnExistingSession() {
                // Given
                ChatSession existingSession = createChatSession(1L, "활성 세션", testRoom);
                when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
                when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
                when(chatSessionRepository.findByRoomAndStatus(testRoom, SessionStatus.OPEN)).thenReturn(Optional.of(existingSession));

                // When
                Optional<ChatSessionResponse> responseOptional = chatSessionService.findActiveSession(testRoom.getCode(), studentUser.getId());

                // Then
                assertThat(responseOptional).isPresent();
                ChatSessionResponse response = responseOptional.get();
                assertThat(response).isNotNull();
                assertThat(response.getSessionId()).isEqualTo(existingSession.getId());
                assertThat(response.getTitle()).isEqualTo(existingSession.getTitle());
                assertThat(response.isActive()).isTrue();
            }

            @Test
            @DisplayName("실패 - 활성 세션이 없을 때 예외 발생")
            void failure_noSessionThrowsException() {
                // Given
                when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
                when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
                when(chatSessionRepository.findByRoomAndStatus(testRoom, SessionStatus.OPEN)).thenReturn(Optional.empty());

                // When & Then
                assertThrows(SessionNotStartedException.class, () -> chatSessionService.findActiveSession(testRoom.getCode(), studentUser.getId()));
            }
        }
    }

    @Nested
    @DisplayName("새 세션 시작 (startNewSession)")
    class StartNewSessionTests {
        @Test
        @DisplayName("성공 - 오늘 진행된 세션이 없을 때 새로운 세션 생성")
        void success_createNewSessionWhenNoneExistsForToday() {
            // Given
            String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String sessionTitle = todayDate + " 수업 기록";
            ChatSession newSession = createChatSession(1L, sessionTitle, testRoom);

            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
            when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
            when(chatSessionRepository.findByRoomAndSessionDate(eq(testRoom), any(LocalDate.class))).thenReturn(Optional.empty());
            when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(newSession);

            // When
            ChatSessionResponse response = chatSessionService.startNewSession(testRoom.getCode(), instructorUser.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isEqualTo(newSession.getId());
            assertThat(response.getTitle()).isEqualTo(sessionTitle);
            assertThat(response.isActive()).isTrue();
            verify(chatSessionRepository).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("실패 - 강사가 아닌 사용자가 세션 시작 시도")
        void failure_nonInstructorCannotStartSession() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));

            // When & Then
            assertThrows(UnauthorizedRoomAccessException.class, () -> chatSessionService.startNewSession(testRoom.getCode(), studentUser.getId()));
            verify(chatSessionRepository, never()).save(any(ChatSession.class));
        }

        @Test
        @DisplayName("실패 - 오늘 이미 활성화된 세션이 존재할 때 예외 발생")
        void failure_throwsExceptionWhenActiveSessionExistsForToday() {
            // Given
            ChatSession activeSession = createChatSession(1L, "Active Session", testRoom); // Status is OPEN
            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
            when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
            when(chatSessionRepository.findByRoomAndSessionDate(eq(testRoom), any(LocalDate.class))).thenReturn(Optional.of(activeSession));

            // When & Then
            assertThrows(IllegalStateException.class, () -> chatSessionService.startNewSession(testRoom.getCode(), instructorUser.getId()));
            verify(chatSessionRepository, never()).save(any(ChatSession.class));
        }
    }

    @Nested
    @DisplayName("세션 엔티티 조회 (findSessionEntity)")
    class FindSessionEntityTests {
        @Test
        @DisplayName("성공")
        void success() {
            // Given
            ChatSession session = createChatSession(1L, "Test Session", testRoom);
            when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(session));

            // When
            ChatSession foundSession = chatSessionService.findSessionEntity(1L);

            // Then
            assertThat(foundSession).isNotNull();
            assertThat(foundSession.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 세션")
        void failure_notFound() {
            // Given
            when(chatSessionRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ChatSessionNotFoundException.class, () -> chatSessionService.findSessionEntity(99L));
        }
    }

    @Nested
    @DisplayName("종료된 세션 목록 조회 (getArchivedSessions)")
    class GetArchivedSessionsTests {
        @Test
        @DisplayName("성공")
        void success() {
            // Given
            ChatSession closedSession1 = createChatSession(1L, "Old Session 1", testRoom);
            closedSession1.close();
            ChatSession closedSession2 = createChatSession(2L, "Old Session 2", testRoom);
            closedSession2.close();

            when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
            when(chatSessionRepository.findByRoomAndStatusOrderBySessionDateDesc(testRoom, SessionStatus.CLOSED))
                    .thenReturn(Arrays.asList(closedSession2, closedSession1));

            // When
            List<ChatSessionResponse> responses = chatSessionService.getArchivedSessions(testRoom.getCode());

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getTitle()).isEqualTo("Old Session 2");
            assertThat(responses.get(1).getTitle()).isEqualTo("Old Session 1");
        }
    }

    @Nested
    @DisplayName("세션 종료 (closeSession)")
    class CloseSessionTests {
        @Test
        @DisplayName("성공")
        void success() {
            // Given
            ChatSession openSession = createChatSession(1L, "Open Session", testRoom);
            when(chatSessionRepository.findById(1L)).thenReturn(Optional.of(openSession));
            when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(openSession);

            // When
            chatSessionService.closeSession(1L);

            // Then
            verify(chatSessionRepository).save(openSession);
            assertThat(openSession.getStatus()).isEqualTo(SessionStatus.CLOSED);
            assertThat(openSession.getEndTime()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 세션")
        void failure_notFound() {
            // Given
            when(chatSessionRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ChatSessionNotFoundException.class, () -> chatSessionService.closeSession(99L));
        }
    }
}
