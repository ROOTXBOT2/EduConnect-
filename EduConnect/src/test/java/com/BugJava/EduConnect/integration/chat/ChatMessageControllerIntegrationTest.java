package com.BugJava.EduConnect.integration.chat;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.chat.domain.ChatMessage;
import com.BugJava.EduConnect.chat.dto.*;
import com.BugJava.EduConnect.chat.enums.MessageType;
import com.BugJava.EduConnect.chat.repository.ChatMessageRepository;
import com.BugJava.EduConnect.chat.service.ChatMessageService;
import com.BugJava.EduConnect.chat.service.ChatSessionService;
import com.BugJava.EduConnect.chat.service.RoomService;
import com.BugJava.EduConnect.integration.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ChatMessageController 통합 테스트")
class ChatMessageControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String instructorToken;
    private Users instructorUser;
    private String studentToken;
    private Users studentUser;
    private RoomResponse testRoomResponse;
    private ChatSessionResponse testChatSessionResponse;

    @BeforeEach
    void setUp() {
        // 강사 유저
        UserAuthInfo instructorAuth = registerAndLoginUser("msg_instructor@example.com", "password", "메시지강사", Role.INSTRUCTOR);
        instructorToken = instructorAuth.accessToken;
        instructorUser = instructorAuth.user;

        // 학생 유저
        UserAuthInfo studentAuth = registerAndLoginUser("msg_student@example.com", "password", "메시지학생", Role.STUDENT);
        studentToken = studentAuth.accessToken;
        studentUser = studentAuth.user;

        // 테스트용 강의실 생성
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setTitle("메시지 테스트 강의실");
        testRoomResponse = roomService.createRoom(instructorUser.getId(), roomRequest);

        // 테스트용 채팅 세션 생성 또는 조회
        Optional<ChatSessionResponse> sessionOptional = chatSessionService.findActiveSession(testRoomResponse.getCode(), instructorUser.getId());
        if (sessionOptional.isPresent()) {
            testChatSessionResponse = sessionOptional.get();
        } else {
            // If no session exists, start a new one (as instructor)
            testChatSessionResponse = chatSessionService.startNewSession(testRoomResponse.getCode(), instructorUser.getId());
        }
    }

    @Nested
    @DisplayName("메시지 목록 조회 (/api/chat/messages/sessions/{sessionId})")
    class GetMessages {

        @BeforeEach
        void setUpMessages() {
            // 메시지 미리 생성
            MessageRequest msg1 = new MessageRequest();
            msg1.setContent("첫 번째 메시지");
            msg1.setMessageType(MessageType.TEXT);
            chatMessageService.saveAndSendMessage(testChatSessionResponse.getSessionId(), instructorUser.getId(), msg1);

            MessageRequest msg2 = new MessageRequest();
            msg2.setContent("두 번째 메시지");
            msg2.setMessageType(MessageType.TEXT);
            chatMessageService.saveAndSendMessage(testChatSessionResponse.getSessionId(), studentUser.getId(), msg2);
        }

        @Test
        @DisplayName("성공 - 인증된 사용자 (강사)가 메시지 조회")
        void success_authenticatedUserGetsMessages() throws Exception {
            mockMvc.perform(get("/api/chat/messages/sessions/" + testChatSessionResponse.getSessionId())
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("채팅 메시지 목록 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].content").value("두 번째 메시지"))
                    .andExpect(jsonPath("$.data[1].content").value("첫 번째 메시지"));
        }

        @Test
        @DisplayName("성공 - 인증된 사용자 (학생)가 메시지 조회")
        void success_studentGetsMessages() throws Exception {
            mockMvc.perform(get("/api/chat/messages/sessions/" + testChatSessionResponse.getSessionId())
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("채팅 메시지 목록 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void failure_unauthorized() throws Exception {
            mockMvc.perform(get("/api/chat/messages/sessions/" + testChatSessionResponse.getSessionId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 세션 (Not Found)")
        void failure_sessionNotFound() throws Exception {
            mockMvc.perform(get("/api/chat/messages/sessions/99999")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("메시지 수정 (/api/chat/messages/{messageId})")
    class UpdateMessage {

        private Long messageIdToUpdate;

        @BeforeEach
        void setUpMessage() {
            MessageRequest msg = new MessageRequest();
            msg.setContent("수정될 메시지");
            msg.setMessageType(MessageType.TEXT);
            messageIdToUpdate = chatMessageService.saveAndSendMessage(testChatSessionResponse.getSessionId(), instructorUser.getId(), msg).getMessageId();
        }

        @Test
        @DisplayName("성공 - 강사가 자신의 메시지 수정")
        void success_instructorUpdatesOwnMessage() throws Exception {
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("수정 완료된 메시지");

            mockMvc.perform(patch("/api/chat/messages/" + messageIdToUpdate)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("메시지가 성공적으로 수정되었습니다."))
                    .andExpect(jsonPath("$.data.content").value("수정 완료된 메시지"))
                    .andExpect(jsonPath("$.data.edited").value(true));
        }

        @Test
        @DisplayName("실패 - 강사가 다른 사용자의 메시지 수정 (Forbidden)")
        void failure_instructorUpdatesOtherUserMessage() throws Exception {
            // 학생이 보낸 메시지 생성
            MessageRequest studentMsg = new MessageRequest();
            studentMsg.setContent("학생 메시지");
            studentMsg.setMessageType(MessageType.TEXT);
            Long studentMessageId = chatMessageService.saveAndSendMessage(testChatSessionResponse.getSessionId(), studentUser.getId(), studentMsg).getMessageId();

            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("강사가 수정하려는 학생 메시지");

            mockMvc.perform(patch("/api/chat/messages/" + studentMessageId)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 학생이 메시지 수정 (Forbidden)")
        void failure_studentUpdatesMessage() throws Exception {
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("학생이 수정하려는 메시지");

            mockMvc.perform(patch("/api/chat/messages/" + messageIdToUpdate)
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void failure_unauthorized() throws Exception {
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("미인증 수정");

            mockMvc.perform(patch("/api/chat/messages/" + messageIdToUpdate)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 메시지 (Not Found)")
        void failure_messageNotFound() throws Exception {
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("없는 메시지 수정");

            mockMvc.perform(patch("/api/chat/messages/99999")
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 (빈 내용)")
        void failure_badRequest() throws Exception {
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent(""); // 빈 내용

            mockMvc.perform(patch("/api/chat/messages/" + messageIdToUpdate)
                            .header("Authorization", "Bearer " + instructorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("메시지 삭제 (/api/chat/messages/{messageId})")
    class DeleteMessage {

        private Long messageIdToDelete;

        @BeforeEach
        void setUpMessage() {
            MessageRequest msg = new MessageRequest();
            msg.setContent("삭제될 메시지");
            msg.setMessageType(MessageType.TEXT);
            messageIdToDelete = chatMessageService.saveAndSendMessage(testChatSessionResponse.getSessionId(), instructorUser.getId(), msg).getMessageId();
        }

        @Test
        @DisplayName("성공 - 강사가 자신의 메시지 삭제")
        void success_instructorDeletesOwnMessage() throws Exception {
            mockMvc.perform(delete("/api/chat/messages/" + messageIdToDelete)
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("메시지가 성공적으로 삭제되었습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist()); // 삭제된 메시지는 반환하지 않음

            // 삭제 확인
            Optional<ChatMessage> deletedMessage = chatMessageRepository.findById(messageIdToDelete);
            assertThat(deletedMessage).isPresent();
            assertThat(deletedMessage.get().isDeleted()).isTrue();
        }

        @Test
        @DisplayName("실패 - 강사가 다른 사용자의 메시지 삭제 (Forbidden)")
        void failure_instructorDeletesOtherUserMessage() throws Exception {
            // 학생이 보낸 메시지 생성
            MessageRequest studentMsg = new MessageRequest();
            studentMsg.setContent("학생 메시지");
            studentMsg.setMessageType(MessageType.TEXT);
            Long studentMessageId = chatMessageService.saveAndSendMessage(testChatSessionResponse.getSessionId(), studentUser.getId(), studentMsg).getMessageId();

            mockMvc.perform(delete("/api/chat/messages/" + studentMessageId)
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 학생이 메시지 삭제 (Forbidden)")
        void failure_studentDeletesMessage() throws Exception {
            mockMvc.perform(delete("/api/chat/messages/" + messageIdToDelete)
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자 (Unauthorized)")
        void failure_unauthorized() throws Exception {
            mockMvc.perform(delete("/api/chat/messages/" + messageIdToDelete))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 메시지 (Not Found)")
        void failure_messageNotFound() throws Exception {
            mockMvc.perform(delete("/api/chat/messages/99999")
                            .header("Authorization", "Bearer " + instructorToken))
                    .andExpect(status().isNotFound());
        }
    }
}