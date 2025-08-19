package com.BugJava.EduConnect.unit.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.domain.ChatCodeBlock;
import com.BugJava.EduConnect.chat.domain.ChatMessage;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.MessageRequest;
import com.BugJava.EduConnect.chat.dto.MessageResponse;
import com.BugJava.EduConnect.chat.dto.MessageUpdateRequest;
import com.BugJava.EduConnect.chat.enums.MessageType;
import com.BugJava.EduConnect.chat.exception.ChatMessageNotFoundException;
import com.BugJava.EduConnect.chat.exception.UnauthorizedMessageAccessException;
import com.BugJava.EduConnect.chat.repository.ChatCodeBlockRepository;
import com.BugJava.EduConnect.chat.repository.ChatMessageRepository;
import com.BugJava.EduConnect.chat.service.ChatMessageService;
import com.BugJava.EduConnect.chat.service.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Field;

import static com.BugJava.EduConnect.util.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageService 테스트")
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatCodeBlockRepository chatCodeBlockRepository;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private Users instructorUser;
    private Users studentUser;
    private Room testRoom;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        instructorUser = createUser(1L, "강사님", Role.INSTRUCTOR);
        studentUser = createUser(2L, "학생님", Role.STUDENT);
        testRoom = createRoom(1L, "ROOM123", "테스트 강의실", instructorUser);
        testSession = createChatSession(1L, "Test Session", testRoom);
    }

    @Nested
    @DisplayName("최신 메시지 조회 (findLatestMessages)")
    class FindLatestMessagesTests {
        @Test
        @DisplayName("성공")
        void success() {
            // Given
            ChatMessage msg1 = createChatMessage(1L, testSession, instructorUser, "Hello", MessageType.TEXT);
            ChatMessage msg2 = createChatMessage(2L, testSession, studentUser, "World", MessageType.TEXT);
            List<ChatMessage> messages = Arrays.asList(msg2, msg1); // 최신순

            when(chatMessageRepository.findBySessionIdWithPaging(any(Long.class), any(Long.class), any(PageRequest.class)))
                    .thenReturn(new SliceImpl<>(messages));

            // When
            List<MessageResponse> responses = chatMessageService.findLatestMessages(testSession.getId(), null, 10);

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getContent()).isEqualTo("World");
            assertThat(responses.get(1).getContent()).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("메시지 전송 (saveAndSendMessage)")
    class SaveAndSendMessageTests {
        @Test
        @DisplayName("성공 - TEXT 타입")
        void success_text() {
            // Given
            MessageRequest request = new MessageRequest();
            request.setMessageType(MessageType.TEXT);
            request.setContent("새로운 텍스트 메시지");

            ChatMessage savedMsg = createChatMessage(1L, testSession, instructorUser, request.getContent(), MessageType.TEXT);

            when(chatSessionService.findSessionEntity(testSession.getId())).thenReturn(testSession);
            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMsg);

            // When
            MessageResponse response = chatMessageService.saveAndSendMessage(testSession.getId(), instructorUser.getId(), request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessageId()).isEqualTo(savedMsg.getId());
            assertThat(response.getContent()).isEqualTo(request.getContent());
            assertThat(response.getMessageType()).isEqualTo(MessageType.TEXT);
        }

        @Test
        @DisplayName("성공 - CODE 타입")
        void success_code() {
            // Given
            MessageRequest request = new MessageRequest();
            request.setMessageType(MessageType.CODE);
            request.setContent("public static void main");
            request.setLanguage("java");

            ChatMessage savedMsg = createChatMessage(1L, testSession, instructorUser, request.getContent(), MessageType.CODE);
            ChatCodeBlock savedCodeBlock = createChatCodeBlock(1L, savedMsg, "java", request.getContent());
            setChatCodeBlockOnMessage(savedMsg, savedCodeBlock); // Set the relationship for the test mock

            when(chatSessionService.findSessionEntity(testSession.getId())).thenReturn(testSession);
            when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMsg);
            when(chatCodeBlockRepository.save(any(ChatCodeBlock.class))).thenReturn(savedCodeBlock);
            

            // When
            MessageResponse response = chatMessageService.saveAndSendMessage(testSession.getId(), instructorUser.getId(), request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessageId()).isEqualTo(savedMsg.getId());
            assertThat(response.getContent()).isEqualTo(request.getContent());
            assertThat(response.getMessageType()).isEqualTo(MessageType.CODE);
            assertThat(response.getLanguage()).isEqualTo("java");
            assertThat(response.getCodeContent()).isEqualTo(request.getContent());
        }

        // Helper method to set ChatCodeBlock on ChatMessage using reflection
        private void setChatCodeBlockOnMessage(ChatMessage message, ChatCodeBlock codeBlock) {
            try {
                Field field = ChatMessage.class.getDeclaredField("chatCodeBlock");
                field.setAccessible(true);
                field.set(message, codeBlock);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to set chatCodeBlock on ChatMessage for testing", e);
            }
        }
    }

    @Nested
    @DisplayName("메시지 수정 (updateMessage)")
    class UpdateMessageTests {
        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Long messageId = 1L;
            ChatMessage existingMsg = createChatMessage(messageId, testSession, instructorUser, "원본 메시지", MessageType.TEXT);
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("수정된 메시지");

            when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMsg));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            MessageResponse response = chatMessageService.updateMessage(messageId, instructorUser.getId(), updateRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEqualTo("수정된 메시지");
            assertThat(response.isEdited()).isTrue();
            verify(chatMessageRepository).save(existingMsg);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 메시지")
        void failure_notFound() {
            // Given
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("수정된 메시지");
            when(chatMessageRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ChatMessageNotFoundException.class, () -> chatMessageService.updateMessage(99L, instructorUser.getId(), updateRequest));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void failure_unauthorized() {
            // Given
            Long messageId = 1L;
            ChatMessage existingMsg = createChatMessage(messageId, testSession, studentUser, "원본 메시지", MessageType.TEXT); // 작성자: studentUser
            MessageUpdateRequest updateRequest = new MessageUpdateRequest();
            updateRequest.setContent("수정된 메시지");

            when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMsg));

            // When & Then
            // instructorUser가 studentUser의 메시지 수정을 시도
            assertThrows(UnauthorizedMessageAccessException.class, () -> chatMessageService.updateMessage(messageId, instructorUser.getId(), updateRequest));
        }
    }

    @Nested
    @DisplayName("메시지 삭제 (deleteMessage)")
    class DeleteMessageTests {
        @Test
        @DisplayName("성공")
        void success() {
            // Given
            Long messageId = 1L;
            ChatMessage existingMsg = createChatMessage(messageId, testSession, instructorUser, "원본 메시지", MessageType.TEXT);

            when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMsg));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            MessageResponse response = chatMessageService.deleteMessage(messageId, instructorUser.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEqualTo("");
            assertThat(response.isDeleted()).isTrue();
            verify(chatMessageRepository).save(existingMsg);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 메시지")
        void failure_notFound() {
            // Given
            when(chatMessageRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ChatMessageNotFoundException.class, () -> chatMessageService.deleteMessage(99L, instructorUser.getId()));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void failure_unauthorized() {
            // Given
            Long messageId = 1L;
            ChatMessage existingMsg = createChatMessage(messageId, testSession, studentUser, "원본 메시지", MessageType.TEXT); // 작성자: studentUser

            when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(existingMsg));

            // When & Then
            // instructorUser가 studentUser의 메시지 삭제를 시도
            assertThrows(UnauthorizedMessageAccessException.class, () -> chatMessageService.deleteMessage(messageId, instructorUser.getId()));
        }
    }
}
