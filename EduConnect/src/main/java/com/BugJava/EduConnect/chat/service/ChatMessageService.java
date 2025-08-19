package com.BugJava.EduConnect.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.domain.ChatAttachment;
import com.BugJava.EduConnect.chat.domain.ChatCodeBlock;
import com.BugJava.EduConnect.chat.domain.ChatMessage;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.dto.MessageRequest;
import com.BugJava.EduConnect.chat.dto.MessageResponse;
import com.BugJava.EduConnect.chat.dto.MessageUpdateRequest;
import com.BugJava.EduConnect.chat.enums.MessageType;
import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.chat.exception.ChatSessionClosedException;
import com.BugJava.EduConnect.chat.exception.ChatMessageNotFoundException;
import com.BugJava.EduConnect.chat.exception.UnauthorizedMessageAccessException;
import com.BugJava.EduConnect.chat.repository.ChatAttachmentRepository;
import com.BugJava.EduConnect.chat.repository.ChatCodeBlockRepository;
import com.BugJava.EduConnect.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatCodeBlockRepository chatCodeBlockRepository;
    private final ChatAttachmentRepository chatAttachmentRepository;
    private final ChatSessionService chatSessionService;
    private final UserRepository userRepository;

    // 특정 채팅 세션의 최신 메시지부터 과거로 역순(내림차순)으로 메시지를 일정 개수만큼 페이징하여 조회
    // 역방향 무한 스크롤(이전 메시지 추가 로딩)용으로 설계
    public List<MessageResponse> findLatestMessages(
            Long sessionId,     // 채팅 세션 ID
            Long lastMessageId, // 마지막으로 받은 메시지 ID (이전 메시지 기준 페이징용, null이면 가장 최신부터)
            int size            // 가져올 메시지 개수
    ) {
        // 세션 존재 여부 확인
        chatSessionService.findSessionEntity(sessionId); // 세션이 없으면 ChatSessionNotFoundException 발생

        // lastMessageId => 클라이언트가 이전에 받은 메시지 중 가장 오래된 메시지의 ID
        // lastMessageId가 없으면 Long.MAX_VALUE로 초기화해서 DB에서 가장 최신 메시지부터 조회
        Long currentLastMessageId = (lastMessageId == null) ? Long.MAX_VALUE : lastMessageId;
        PageRequest pageRequest = PageRequest.of(0, size);

        // lastMessageId 기준으로 그 이전 메시지들을 최신 순으로 가져옴
        Slice<ChatMessage> messageSlice = chatMessageRepository.findBySessionIdWithPaging(sessionId, currentLastMessageId, pageRequest);

        // 조회된 ChatMessage 엔티티 리스트를 스트림으로 돌면서 mapToMessageResponse 메서드로 MessageResponse DTO로 변환
        // DTO는 클라이언트가 필요한 정보(내용, 발신자, 타입, 시간 등)를 포함
        // 변환된 메시지 DTO 리스트를 반환
        return messageSlice.getContent().stream()
                .map(message -> mapToMessageResponse(message))
                .collect(Collectors.toList());
    }

    // 새로운 채팅 메시지를 저장하고, 메시지 전송에 필요한 DTO로 변환
    @Transactional
    public MessageResponse saveAndSendMessage(
            Long sessionId,               // 메시지가 속한 채팅 세션 ID
            Long senderId,                // 메시지 발신자 ID
            MessageRequest messageRequest // 메시지 내용 및 타입 등이 담긴 요청 DTO
    ) {
        ChatSession chatSession = chatSessionService.findSessionEntity(sessionId);
        // 세션이 닫혀있는지 확인
        if (chatSession.getStatus() == SessionStatus.CLOSED) {
            throw new ChatSessionClosedException();
        }
        Users sender = userRepository.findById(senderId) // userId로 조회
                .orElseThrow(() -> new UserNotFoundException("메시지 발신 사용자를 찾을 수 없습니다."));

        // 강사 또는 학생만 메시지를 보낼 수 있도록 권한 검증
        if (sender.getRole() != Role.INSTRUCTOR && sender.getRole() != Role.STUDENT) {
            throw new UnauthorizedMessageAccessException("강사 또는 학생만 메시지를 보낼 수 있습니다.");
        }

        // CODE 타입 메시지일 경우 language 필드가 비어있는지 검증
        if (messageRequest.getMessageType() == MessageType.CODE) {
            if (messageRequest.getLanguage() == null || messageRequest.getLanguage().isBlank()) {
                throw new IllegalArgumentException("코드 메시지를 보내려면 언어를 지정해야 합니다.");
            }
        }

        

        // 메시지 엔티티 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatSession(chatSession)
                .sender(sender) // Users 엔티티 사용
                .content(messageRequest.getContent())
                .messageType(messageRequest.getMessageType())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 메시지 타입이 코드일 경우, 코드블럭 엔티티도 함께 저장
        if (messageRequest.getMessageType() == MessageType.CODE) {
            ChatCodeBlock codeBlock = ChatCodeBlock.builder()
                    .chatMessage(savedMessage)
                    .language(messageRequest.getLanguage())
                    .codeContent(messageRequest.getContent())
                    .build();
            chatCodeBlockRepository.save(codeBlock);
            savedMessage.setChatCodeBlock(codeBlock); // Add this line
        } else if (messageRequest.getMessageType() == MessageType.FILE || messageRequest.getMessageType() == MessageType.IMAGE) {
            // TODO: 파일 업로드 로직 구현 (MinIO 또는 S3 연동)
        }

        // 저장된 메시지를 MessageResponse DTO로 변환하여 반환
        return mapToMessageResponse(savedMessage);
    }

    // 기존 메시지를 수정
    @Transactional
    public MessageResponse updateMessage(
            Long messageId,                     // 수정할 메시지 ID
            Long editorId,                      // 수정 요청자(사용자) ID
            MessageUpdateRequest updateRequest  // 수정할 내용 (문자열, 언어 등)
    ) {
        // 메시지 존재 여부 확인
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(ChatMessageNotFoundException::new);

        // 세션이 닫혀있는지 확인
        if (message.getChatSession().getStatus() == SessionStatus.CLOSED) {
            throw new ChatSessionClosedException();
        }

        // 요청자가 메시지 작성자인지 권한 확인
        if (!message.getSender().getId().equals(editorId)) { // userId로 비교
            throw new UnauthorizedMessageAccessException("메시지를 수정할 권한이 없습니다.");
        }

        

        // 메시지 내용 업데이트 후 저장
        message.update(updateRequest.getContent());
        ChatMessage updatedMessage = chatMessageRepository.save(message);

        // 메시지 타입이 코드인 경우, 관련 코드블럭 내용 및 언어도 함께 업데이트
        if (updatedMessage.getMessageType() == MessageType.CODE) {
            chatCodeBlockRepository.findByChatMessage(updatedMessage).ifPresent(codeBlock -> {
                codeBlock.updateCodeContent(updateRequest.getContent());
                // language 업데이트 로직 추가
                if (updateRequest.getLanguage() != null && !updateRequest.getLanguage().isEmpty()) {
                    codeBlock.updateLanguage(updateRequest.getLanguage());
                }
                chatCodeBlockRepository.save(codeBlock);
            });
        }

        // 수정된 메시지를 MessageResponse DTO로 반환
        return mapToMessageResponse(updatedMessage);
    }
    
    // 메시지를 삭제 처리
    @Transactional
    public MessageResponse deleteMessage(
            Long messageId, // 삭제할 메시지 ID
            Long deleterId  // 삭제 요청자(사용자) ID
    ) {
        // 메시지 존재 여부 확인
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(ChatMessageNotFoundException::new);

        // 세션이 닫혀있는지 확인
        if (message.getChatSession().getStatus() == SessionStatus.CLOSED) {
            throw new ChatSessionClosedException();
        }

        // 요청자가 작성자인지 권한 확인
        if (!message.getSender().getId().equals(deleterId)) {
            throw new UnauthorizedMessageAccessException("메시지를 삭제할 권한이 없습니다.");
        }

        // 메시지를 논리 삭제 처리 (deleted 플래그 설정)
        message.delete();
        ChatMessage deletedMessage = chatMessageRepository.save(message);

        // 삭제 처리된 메시지 상태를 반영한 MessageResponse DTO 반환
        return mapToMessageResponse(deletedMessage);
    }

    // ChatMessage 엔티티를 MessageResponse DTO로 변환
    // 기본 메시지 정보 세팅
    private MessageResponse mapToMessageResponse(ChatMessage message) {
        MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                .messageId(message.getId())
                .sessionId(message.getChatSession().getId())
                .senderId(message.getSender().getId())
                .sender(message.getSender().getName()) // sender의 이름을 사용
                .content(message.isDeleted() ? "" : message.getContent())
                .messageType(message.getMessageType())
                .timestamp(message.getCreatedAt())
                .edited(message.isEdited())
                .deleted(message.isDeleted());

        // 메시지 타입에 따라 추가 정보 설정 (@EntityGraph로 이미 조회되었으므로 추가 쿼리 없음)
        if (message.getMessageType() == MessageType.CODE && message.getChatCodeBlock() != null) {
            ChatCodeBlock codeBlock = message.getChatCodeBlock();
            builder.language(codeBlock.getLanguage())
                    .codeContent(codeBlock.getCodeContent());
        } else if ((message.getMessageType() == MessageType.IMAGE || message.getMessageType() == MessageType.FILE) && message.getChatAttachment() != null) {
            ChatAttachment attachment = message.getChatAttachment();
            builder.fileName(attachment.getFileName())
                    .fileUrl(attachment.getFileUrl());
        }

        // 완성된 MessageResponse DTO 객체 반환
        return builder.build();
    }
}
