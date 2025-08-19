package com.BugJava.EduConnect.integration.chat;

import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.chat.dto.EnrollmentRequest;
import com.BugJava.EduConnect.chat.dto.RoomRequest;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.service.RoomService;
import com.BugJava.EduConnect.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.annotation.DirtiesContext;

@DisplayName("EnrollmentController 통합 테스트")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EnrollmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoomService roomService;

    private String instructorToken;
    private String studentToken;
    private Long studentId;
    private RoomResponse testRoom;

    @BeforeEach
    void setUp() {
        UserAuthInfo instructorAuth = registerAndLoginUser("instructor@example.com", "password", "강사님", Role.INSTRUCTOR);
        instructorToken = instructorAuth.accessToken;

        UserAuthInfo studentAuth = registerAndLoginUser("student@example.com", "password", "학생님", Role.STUDENT);
        studentToken = studentAuth.accessToken;
        studentId = studentAuth.user.getId();

        // 테스트용 강의실 생성
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setTitle("참여 테스트 강의실");
        testRoom = roomService.createRoom(instructorAuth.user.getId(), roomRequest);
    }

    @Nested
    @DisplayName("강의실 참여 (/api/enrollments)")
    class EnrollRoom {

        @Test
        @DisplayName("성공 - 학생이 참여")
        void enrollRoom_success() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest();
            request.setRoomCode(testRoom.getCode());

            mockMvc.perform(post("/api/chat/enrollments")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("강의실 참여에 성공했습니다."))
                    .andExpect(jsonPath("$.data.room.code").value(testRoom.getCode()));
        }

        @Test
        @DisplayName("실패 - 이미 참여한 강의실에 재참여")
        void enrollRoom_fail_alreadyEnrolled() throws Exception {
            // 먼저 한번 참여
            EnrollmentRequest request = new EnrollmentRequest();
            request.setRoomCode(testRoom.getCode());
            mockMvc.perform(post("/api/chat/enrollments")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 다시 참여 시도 (409 Conflict 예상)
            mockMvc.perform(post("/api/chat/enrollments")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 코드로 참여")
        void enrollRoom_fail_roomNotFound() throws Exception {
            EnrollmentRequest request = new EnrollmentRequest();
            request.setRoomCode("NONEXISTENT_CODE");

            mockMvc.perform(post("/api/chat/enrollments")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("참여한 강의실 목록 조회 (/api/enrollments)")
    class GetEnrolledRooms {

        @Test
        @DisplayName("성공")
        void getEnrolledRooms_success() throws Exception {
            // 먼저 강의실에 참여
            EnrollmentRequest request = new EnrollmentRequest();
            request.setRoomCode(testRoom.getCode());
            mockMvc.perform(post("/api/chat/enrollments")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // 목록 조회
            mockMvc.perform(get("/api/chat/enrollments/my-rooms")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.rooms").isArray()) // Changed from $.data to $.data.rooms
                    .andExpect(jsonPath("$.data.rooms.length()").value(1)) // Changed from $.data to $.data.rooms
                    .andExpect(jsonPath("$.data.rooms[0].code").value(testRoom.getCode())) // Changed from $.data to $.data.rooms
                    .andExpect(jsonPath("$.data.sessionCloseCutoffTime").isString()); // Verify cutoff time is present
        }
    }

    @Nested
    @DisplayName("강의실 참여 취소 (/api/chat/enrollments/rooms/{roomCode})")
    class UnenrollRoom {

        @Test
        @DisplayName("성공 - 학생이 강의실 참여 취소")
        void unenrollRoom_success() throws Exception {
            // 먼저 강의실에 참여
            EnrollmentRequest request = new EnrollmentRequest();
            request.setRoomCode(testRoom.getCode());
            mockMvc.perform(post("/api/chat/enrollments")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // 참여 취소
            mockMvc.perform(delete("/api/chat/enrollments/rooms/" + testRoom.getCode())
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("강의실 참여가 성공적으로 취소되었습니다."));

            // 참여 취소 후 다시 조회 시 목록에 없는지 확인
            mockMvc.perform(get("/api/chat/enrollments/my-rooms")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.rooms").isArray())
                    .andExpect(jsonPath("$.data.rooms.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 참여하지 않은 강의실 참여 취소")
        void unenrollRoom_fail_notEnrolled() throws Exception {
            mockMvc.perform(delete("/api/chat/enrollments/rooms/" + testRoom.getCode())
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isBadRequest()); // 또는 NOT_FOUND, CONFLICT 등 적절한 상태 코드
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실 참여 취소")
        void unenrollRoom_fail_roomNotFound() throws Exception {
            mockMvc.perform(delete("/api/chat/enrollments/rooms/NONEXISTENT_CODE")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void unenrollRoom_fail_unauthorized() throws Exception {
            mockMvc.perform(delete("/api/chat/enrollments/rooms/" + testRoom.getCode()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 강사가 참여 취소 (Forbidden)")
        void unenrollRoom_fail_byInstructor() throws Exception {
            mockMvc.perform(delete("/api/chat/enrollments/rooms/" + testRoom.getCode())
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isForbidden());
        }
    }
}
