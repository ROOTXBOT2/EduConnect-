package com.BugJava.EduConnect.unit.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.domain.Enrollment;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.EnrollmentRequest;
import com.BugJava.EduConnect.chat.dto.EnrollmentResponse;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.chat.exception.AlreadyEnrolledException;
import com.BugJava.EduConnect.chat.exception.RoomNotFoundException;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import com.BugJava.EduConnect.chat.repository.EnrollmentRepository;
import com.BugJava.EduConnect.chat.service.EnrollmentService;
import com.BugJava.EduConnect.chat.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.BugJava.EduConnect.util.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.BugJava.EduConnect.chat.dto.EnrolledRoomListResponse; // New import
import com.BugJava.EduConnect.chat.config.ChatProperties; // New import
import java.time.LocalTime; // Needed for mocking ChatProperties

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService 테스트")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatProperties chatProperties; // New mock

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Users studentUser;
    private Users instructorUser;
    private Room testRoom;
    private EnrollmentRequest enrollmentRequest;

    @BeforeEach
    void setUp() {
        studentUser = createUser(1L, "학생님", Role.STUDENT);
        instructorUser = createUser(2L, "강사님", Role.INSTRUCTOR);
        testRoom = createRoom(1L, "ROOM123", "테스트 강의실", instructorUser);

        enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setRoomCode("ROOM123");
    }

    @Nested
    @DisplayName("강의실 참여 (enrollRoom)")
    class EnrollRoomTests {

        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Enrollment savedEnrollment = createEnrollment(1L, studentUser, testRoom);

            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(roomService.findRoomEntityByCode(enrollmentRequest.getRoomCode())).thenReturn(testRoom);
            when(enrollmentRepository.existsByUserAndRoom(studentUser, testRoom)).thenReturn(false);
            when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);

            // When
            EnrollmentResponse response = enrollmentService.enrollRoom(studentUser.getId(), enrollmentRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getEnrollmentId()).isEqualTo(savedEnrollment.getId());
            assertThat(response.getUserEmail()).isEqualTo(studentUser.getEmail());
            assertThat(response.getRoom().getCode()).isEqualTo(testRoom.getCode());
            verify(userRepository).findById(studentUser.getId());
            verify(roomService).findRoomEntityByCode(enrollmentRequest.getRoomCode());
            verify(enrollmentRepository).existsByUserAndRoom(studentUser, testRoom);
            verify(enrollmentRepository).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void failure_userNotFound() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> enrollmentService.enrollRoom(studentUser.getId(), enrollmentRequest));
            verify(userRepository).findById(studentUser.getId());
            verify(roomService, never()).findRoomEntityByCode(any());
            verify(enrollmentRepository, never()).existsByUserAndRoom(any(), any());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실")
        void failure_roomNotFound() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(roomService.findRoomEntityByCode(enrollmentRequest.getRoomCode())).thenThrow(RoomNotFoundException.class);

            // When & Then
            assertThrows(RoomNotFoundException.class, () -> enrollmentService.enrollRoom(studentUser.getId(), enrollmentRequest));
            verify(userRepository).findById(studentUser.getId());
            verify(roomService).findRoomEntityByCode(enrollmentRequest.getRoomCode());
            verify(enrollmentRepository, never()).existsByUserAndRoom(any(), any());
            verify(enrollmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 이미 참여 중인 강의실")
        void failure_alreadyEnrolled() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(roomService.findRoomEntityByCode(enrollmentRequest.getRoomCode())).thenReturn(testRoom);
            when(enrollmentRepository.existsByUserAndRoom(studentUser, testRoom)).thenReturn(true);

            // When & Then
            assertThrows(AlreadyEnrolledException.class, () -> enrollmentService.enrollRoom(studentUser.getId(), enrollmentRequest));
            verify(userRepository).findById(studentUser.getId());
            verify(roomService).findRoomEntityByCode(enrollmentRequest.getRoomCode());
            verify(enrollmentRepository).existsByUserAndRoom(studentUser, testRoom);
            verify(enrollmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("참여 중인 강의실 목록 조회 (getEnrolledRooms)")
    class GetEnrolledRoomsTests {

        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Room anotherRoom = createRoom(2L, "ROOM456", "다른 강의실", instructorUser);
            Enrollment enrollment1 = createEnrollment(1L, studentUser, testRoom);
            Enrollment enrollment2 = createEnrollment(2L, studentUser, anotherRoom);

            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(enrollmentRepository.findByUser(studentUser)).thenReturn(Arrays.asList(enrollment1, enrollment2));
            // Mock chatProperties.getCloseCutoffTime() if needed for EnrollmentService.getEnrolledRooms
            when(chatProperties.getCloseCutoffTime()).thenReturn(LocalTime.of(18, 10)); // Example if chatProperties is mocked

            // When
            EnrolledRoomListResponse response = enrollmentService.getEnrolledRooms(studentUser.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getRooms()).hasSize(2);
            assertThat(response.getRooms().get(0).getCode()).isEqualTo(testRoom.getCode());
            assertThat(response.getRooms().get(1).getCode()).isEqualTo(anotherRoom.getCode());
            assertThat(response.getSessionCloseCutoffTime()).isNotNull(); // Verify cutoff time is present
            verify(userRepository).findById(studentUser.getId());
            verify(enrollmentRepository).findByUser(studentUser);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void failure_userNotFound() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> enrollmentService.getEnrolledRooms(studentUser.getId()));
            verify(userRepository).findById(studentUser.getId());
            verify(enrollmentRepository, never()).findByUser(any());
        }
    }

    @Nested
    @DisplayName("강의실 참여 취소 (unenrollRoom)")
    class UnenrollRoomTests {

        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Enrollment existingEnrollment = createEnrollment(1L, studentUser, testRoom);
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
            when(enrollmentRepository.findByUserAndRoom(studentUser, testRoom)).thenReturn(Optional.of(existingEnrollment));
            doNothing().when(enrollmentRepository).delete(any(Enrollment.class));

            // When
            enrollmentService.unenrollRoom(studentUser.getId(), testRoom.getCode());

            // Then
            verify(userRepository).findById(studentUser.getId());
            verify(roomService).findRoomEntityByCode(testRoom.getCode());
            verify(enrollmentRepository).findByUserAndRoom(studentUser, testRoom);
            verify(enrollmentRepository).delete(existingEnrollment);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void failure_userNotFound() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> enrollmentService.unenrollRoom(studentUser.getId(), testRoom.getCode()));
            verify(userRepository).findById(studentUser.getId());
            verify(roomService, never()).findRoomEntityByCode(any());
            verify(enrollmentRepository, never()).findByUserAndRoom(any(), any());
            verify(enrollmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실")
        void failure_roomNotFound() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(roomService.findRoomEntityByCode("NONEXISTENT")).thenThrow(RoomNotFoundException.class);

            // When & Then
            assertThrows(RoomNotFoundException.class, () -> enrollmentService.unenrollRoom(studentUser.getId(), "NONEXISTENT"));
            verify(userRepository).findById(studentUser.getId());
            verify(roomService).findRoomEntityByCode("NONEXISTENT");
            verify(enrollmentRepository, never()).findByUserAndRoom(any(), any());
            verify(enrollmentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("실패 - 참여하고 있지 않은 강의실")
        void failure_notEnrolled() {
            // Given
            when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
            when(roomService.findRoomEntityByCode(testRoom.getCode())).thenReturn(testRoom);
            when(enrollmentRepository.findByUserAndRoom(studentUser, testRoom)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> enrollmentService.unenrollRoom(studentUser.getId(), testRoom.getCode()));
            verify(userRepository).findById(studentUser.getId());
            verify(roomService).findRoomEntityByCode(testRoom.getCode());
            verify(enrollmentRepository).findByUserAndRoom(studentUser, testRoom);
            verify(enrollmentRepository, never()).delete(any());
        }
    }
}
