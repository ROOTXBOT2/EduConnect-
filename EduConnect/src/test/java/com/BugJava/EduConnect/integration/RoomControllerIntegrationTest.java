package com.BugJava.EduConnect.integration;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.chat.dto.RoomRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.annotation.DirtiesContext;

@DisplayName("RoomController 통합 테스트")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RoomControllerIntegrationTest extends BaseIntegrationTest {

    private String instructorToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // 강사 유저
        UserAuthInfo instructorAuth = registerAndLoginUser("instructor@example.com", "password", "강사님", Role.INSTRUCTOR);
        instructorToken = instructorAuth.accessToken;

        // 학생 유저
        UserAuthInfo studentAuth = registerAndLoginUser("student@example.com", "password", "학생님", Role.STUDENT);
        studentToken = studentAuth.accessToken;
    }

    @Nested
    @DisplayName("강의실 생성 (/api/rooms)")
    class CreateRoom {

        @Test
        @DisplayName("성공 - 강사가 생성")
        void createRoom_success_byInstructor() throws Exception {
            RoomRequest request = new RoomRequest();
            request.setTitle("새로운 스프링 강의");

            mockMvc.perform(post("/api/chat/rooms")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("채팅방이 성공적으로 생성되었습니다."))
                    .andExpect(jsonPath("$.data.title").value("새로운 스프링 강의"))
                    .andExpect(jsonPath("$.data.code").exists());
        }

        @Test
        @DisplayName("실패 - 학생이 생성 (Forbidden)")
        void createRoom_fail_byStudent() throws Exception {
            RoomRequest request = new RoomRequest();
            request.setTitle("학생이 만드는 강의");

            mockMvc.perform(post("/api/chat/rooms")
                            .header("Authorization", "Bearer " + studentToken) // 학생 토큰 사용
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void createRoom_fail_unauthorized() throws Exception {
            RoomRequest request = new RoomRequest();
            request.setTitle("미인증 강의");

            mockMvc.perform(post("/api/chat/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 (제목 없음)")
        void createRoom_fail_badRequest() throws Exception {
            RoomRequest request = new RoomRequest();
            request.setTitle(""); // 빈 제목

            mockMvc.perform(post("/api/chat/rooms")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("강의실 수정 (/api/chat/rooms/{code})")
    class UpdateRoom {

        private String roomCode;

        @BeforeEach
        void setUpRoom() throws Exception {
            RoomRequest createRequest = new RoomRequest();
            createRequest.setTitle("수정 전 강의실");
            String responseContent = mockMvc.perform(post("/api/chat/rooms")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            roomCode = objectMapper.readTree(responseContent).get("data").get("code").asText();
        }

        @Test
        @DisplayName("성공 - 강사가 강의실 제목 수정")
        void updateRoom_success_byInstructor() throws Exception {
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle("수정 후 강의실");

            mockMvc.perform(patch("/api/chat/rooms/" + roomCode)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("채팅방이 성공적으로 업데이트되었습니다."))
                    .andExpect(jsonPath("$.data.title").value("수정 후 강의실"))
                    .andExpect(jsonPath("$.data.code").value(roomCode));
        }

        @Test
        @DisplayName("실패 - 학생이 강의실 수정 (Forbidden)")
        void updateRoom_fail_byStudent() throws Exception {
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle("학생이 수정하는 강의실");

            mockMvc.perform(patch("/api/chat/rooms/" + roomCode)
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void updateRoom_fail_unauthorized() throws Exception {
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle("미인증 수정");

            mockMvc.perform(patch("/api/chat/rooms/" + roomCode)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실 수정 (Not Found)")
        void updateRoom_fail_notFound() throws Exception {
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle("없는 강의실 수정");

            mockMvc.perform(patch("/api/chat/rooms/nonexistentCode")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 (제목 없음)")
        void updateRoom_fail_badRequest() throws Exception {
            RoomRequest updateRequest = new RoomRequest();
            updateRequest.setTitle(""); // 빈 제목

            mockMvc.perform(patch("/api/chat/rooms/" + roomCode)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("강의실 삭제 (/api/chat/rooms/{code})")
    class DeleteRoom {

        private String roomCode;

        @BeforeEach
        void setUpRoom() throws Exception {
            RoomRequest createRequest = new RoomRequest();
            createRequest.setTitle("삭제할 강의실");
            String responseContent = mockMvc.perform(post("/api/chat/rooms")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            roomCode = objectMapper.readTree(responseContent).get("data").get("code").asText();
        }

        @Test
        @DisplayName("성공 - 강사가 강의실 삭제")
        void deleteRoom_success_byInstructor() throws Exception {
            mockMvc.perform(delete("/api/chat/rooms/" + roomCode)
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("채팅방이 성공적으로 삭제되었습니다."));

            // 삭제 후 조회 시 Not Found 확인
            mockMvc.perform(get("/api/chat/rooms/" + roomCode)
                            .header("Authorization", "Bearer " + instructorToken)) // 조회는 인증 필요
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 학생이 강의실 삭제 (Forbidden)")
        void deleteRoom_fail_byStudent() throws Exception {
            mockMvc.perform(delete("/api/chat/rooms/" + roomCode)
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void deleteRoom_fail_unauthorized() throws Exception {
            mockMvc.perform(delete("/api/chat/rooms/" + roomCode))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의실 삭제 (Not Found)")
        void deleteRoom_fail_notFound() throws Exception {
            mockMvc.perform(delete("/api/chat/rooms/nonexistentCode")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("강의실 목록 조회 (/api/chat/rooms)")
    class GetRooms {

        @Test
        @DisplayName("성공 - 강사가 자신의 강의실 목록 조회")
        void getRooms_success_byInstructor() throws Exception {
            // 테스트를 위해 강의실 여러 개 생성
            RoomRequest request1 = new RoomRequest();
            request1.setTitle("강의실 A");
            mockMvc.perform(post("/api/chat/rooms")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isCreated());

            RoomRequest request2 = new RoomRequest();
            request2.setTitle("강의실 B");
            mockMvc.perform(post("/api/chat/rooms")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isCreated());

            // 강의실 목록 조회
            mockMvc.perform(get("/api/chat/rooms")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("강의실 목록 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data.rooms").isArray())
                    .andExpect(jsonPath("$.data.rooms.length()").value(2))
                    .andExpect(jsonPath("$.data.rooms[0].title").value("강의실 A"))
                    .andExpect(jsonPath("$.data.rooms[1].title").value("강의실 B"))
                    .andExpect(jsonPath("$.data.sessionCloseCutoffTime").exists());
        }

        @Test
        @DisplayName("실패 - 학생이 강의실 목록 조회 (Forbidden)")
        void getRooms_fail_byStudent() throws Exception {
            mockMvc.perform(get("/api/chat/rooms")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void getRooms_fail_unauthorized() throws Exception {
            mockMvc.perform(get("/api/chat/rooms"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
