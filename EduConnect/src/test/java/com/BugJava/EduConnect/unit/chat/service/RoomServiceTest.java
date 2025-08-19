package com.BugJava.EduConnect.unit.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.config.ChatProperties;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.RoomRequest;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.chat.exception.RoomNotFoundException;
import com.BugJava.EduConnect.chat.exception.UnauthorizedRoomAccessException;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import com.BugJava.EduConnect.chat.repository.RoomRepository;
import com.BugJava.EduConnect.chat.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static com.BugJava.EduConnect.util.TestUtils.createRoom;
import static com.BugJava.EduConnect.util.TestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.BugJava.EduConnect.chat.dto.RoomListResponse; // New import

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomService 테스트")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatProperties chatProperties; // New mock

    @InjectMocks
    private RoomService roomService;

    private Users instructorUser;
    private Users otherInstructorUser;
    private RoomRequest roomRequest;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        instructorUser = createUser(1L, "강사님", Role.INSTRUCTOR);
        otherInstructorUser = createUser(2L, "다른강사님", Role.INSTRUCTOR);
        roomRequest = new RoomRequest();
        roomRequest.setTitle("테스트 강의실");
        testRoom = createRoom(1L, "testcode", "테스트 강의실", instructorUser);
    }

    @Nested
    @DisplayName("강의실 생성 (createRoom)")
    class CreateRoomTests {

        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Room savedRoom = createRoom(1L, "testcode", "테스트 강의실", instructorUser);
            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
            when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

            // When
            RoomResponse response = roomService.createRoom(instructorUser.getId(), roomRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo(savedRoom.getTitle());
            assertThat(response.getCode()).isEqualTo(savedRoom.getCode());
            verify(userRepository).findById(instructorUser.getId());
            verify(roomRepository).save(any(Room.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강사")
        void failure_instructorNotFound() {
            // Given
            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> roomService.createRoom(instructorUser.getId(), roomRequest));
            verify(userRepository).findById(instructorUser.getId());
            verify(roomRepository, never()).save(any(Room.class));
        }

        
    }

    @Nested
    @DisplayName("강의실 코드로 조회 (findRoomByCode)")
    class FindRoomByCodeTests {

        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Room room = createRoom(1L, "testcode", "테스트 강의실", instructorUser);
            when(roomRepository.findByCode("testcode")).thenReturn(Optional.of(room));

            // When
            RoomResponse response = roomService.findRoomByCode("testcode");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo(room.getTitle());
            assertThat(response.getCode()).isEqualTo(room.getCode());
            verify(roomRepository).findByCode("testcode");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실")
        void failure_roomNotFound() {
            // Given
            when(roomRepository.findByCode("nonexistent")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RoomNotFoundException.class, () -> roomService.findRoomByCode("nonexistent"));
            verify(roomRepository).findByCode("nonexistent");
        }
    }

    @Nested
    @DisplayName("강의실 엔티티 코드로 조회 (findRoomEntityByCode)")
    class FindRoomEntityByCodeTests {

        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Room room = createRoom(1L, "testcode", "테스트 강의실", instructorUser);
            when(roomRepository.findByCode("testcode")).thenReturn(Optional.of(room));

            // When
            Room foundRoom = roomService.findRoomEntityByCode("testcode");

            // Then
            assertThat(foundRoom).isNotNull();
            assertThat(foundRoom.getCode()).isEqualTo(room.getCode());
            verify(roomRepository).findByCode("testcode");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실")
        void failure_roomNotFound() {
            // Given
            when(roomRepository.findByCode("nonexistent")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RoomNotFoundException.class, () -> roomService.findRoomEntityByCode("nonexistent"));
            verify(roomRepository).findByCode("nonexistent");
        }
    }

    @Nested
    @DisplayName("강의실 업데이트 (updateRoom)")
    class UpdateRoomTests {

        @Test
        @DisplayName("성공 - 강사가 강의실 제목 수정")
        void success_updateTitleByInstructor() {
            // Given
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle("업데이트된 강의실 제목");
            when(roomRepository.findByCode(testRoom.getCode())).thenReturn(Optional.of(testRoom));
            when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

            // When
            RoomResponse response = roomService.updateRoom(testRoom.getCode(), instructorUser.getId(), updateRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("업데이트된 강의실 제목");
            verify(roomRepository).findByCode(testRoom.getCode());
            verify(roomRepository).save(testRoom);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실")
        void failure_roomNotFound() {
            // Given
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle("업데이트된 강의실 제목");
            when(roomRepository.findByCode("nonexistent")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom("nonexistent", instructorUser.getId(), updateRequest));
            verify(roomRepository).findByCode("nonexistent");
            verify(roomRepository, never()).save(any(Room.class));
        }

        @Test
        @DisplayName("실패 - 권한 없음 (다른 강사)")
        void failure_unauthorized_otherInstructor() {
            // Given
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle("업데이트된 강의실 제목");
            when(roomRepository.findByCode(testRoom.getCode())).thenReturn(Optional.of(testRoom));

            // When & Then
            assertThrows(UnauthorizedRoomAccessException.class, () -> roomService.updateRoom(testRoom.getCode(), otherInstructorUser.getId(), updateRequest));
            verify(roomRepository).findByCode(testRoom.getCode());
            verify(roomRepository, never()).save(any(Room.class));
        }
    }

    @Nested
    @DisplayName("강의실 삭제 (deleteRoom)")
    class DeleteRoomTests {

        @Test
        @DisplayName("성공 - 강사가 강의실 삭제")
        void success_deleteRoomByInstructor() {
            // Given
            when(roomRepository.findByCode(testRoom.getCode())).thenReturn(Optional.of(testRoom));
            doNothing().when(roomRepository).delete(any(Room.class));

            // When
            roomService.deleteRoom(testRoom.getCode(), instructorUser.getId());

            // Then
            verify(roomRepository).findByCode(testRoom.getCode());
            verify(roomRepository).delete(testRoom);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실")
        void failure_roomNotFound() {
            // Given
            when(roomRepository.findByCode("nonexistent")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom("nonexistent", instructorUser.getId()));
            verify(roomRepository).findByCode("nonexistent");
            verify(roomRepository, never()).delete(any(Room.class));
        }

        @Test
        @DisplayName("실패 - 권한 없음 (다른 강사)")
        void failure_unauthorized_otherInstructor() {
            // Given
            when(roomRepository.findByCode(testRoom.getCode())).thenReturn(Optional.of(testRoom));

            // When & Then
            assertThrows(UnauthorizedRoomAccessException.class, () -> roomService.deleteRoom(testRoom.getCode(), otherInstructorUser.getId()));
            verify(roomRepository).findByCode(testRoom.getCode());
            verify(roomRepository, never()).delete(any(Room.class));
        }
    }

    @Nested
    @DisplayName("강사별 강의실 목록 조회 (getRoomsByInstructor)")
    class GetRoomsByInstructorTests {

        @Test
        @DisplayName("성공 - 강사가 자신의 강의실 목록 조회")
        void success_getRoomsByInstructor() {
            // Given
            Room room1 = createRoom(1L, "code1", "강의실1", instructorUser);
            Room room2 = createRoom(2L, "code2", "강의실2", instructorUser);
            List<Room> rooms = Arrays.asList(room1, room2);

            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
            when(roomRepository.findByInstructor(instructorUser)).thenReturn(rooms);
            when(chatProperties.getCloseCutoffTime()).thenReturn(LocalTime.of(18, 10)); // Mock cutoff time

            // When
            RoomListResponse response = roomService.getRoomsByInstructor(instructorUser.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getRooms()).hasSize(2);
            assertThat(response.getRooms().get(0).getTitle()).isEqualTo("강의실1");
            assertThat(response.getRooms().get(1).getTitle()).isEqualTo("강의실2");
            assertThat(response.getSessionCloseCutoffTime()).isEqualTo("18:10"); // Verify cutoff time is present
            verify(userRepository).findById(instructorUser.getId());
            verify(roomRepository).findByInstructor(instructorUser);
        }

        @Test
        @DisplayName("성공 - 강사에게 강의실이 없는 경우")
        void success_noRoomsForInstructor() {
            // Given
            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
            when(roomRepository.findByInstructor(instructorUser)).thenReturn(Collections.emptyList());
            when(chatProperties.getCloseCutoffTime()).thenReturn(LocalTime.of(18, 10)); // Mock cutoff time

            // When
            RoomListResponse response = roomService.getRoomsByInstructor(instructorUser.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getRooms()).isEmpty();
            assertThat(response.getSessionCloseCutoffTime()).isNotNull(); // Verify cutoff time is present
            verify(userRepository).findById(instructorUser.getId());
            verify(roomRepository).findByInstructor(instructorUser);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강사")
        void failure_instructorNotFound() {
            // Given
            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> roomService.getRoomsByInstructor(instructorUser.getId()));
            verify(userRepository).findById(instructorUser.getId());
            verify(roomRepository, never()).findByInstructor(any(Users.class));
        }
    }
}

