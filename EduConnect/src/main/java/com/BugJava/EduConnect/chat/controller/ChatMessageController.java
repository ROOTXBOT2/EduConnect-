package com.BugJava.EduConnect.chat.controller;

import com.BugJava.EduConnect.chat.dto.MessageRequest;
import com.BugJava.EduConnect.chat.dto.MessageResponse;
import com.BugJava.EduConnect.chat.dto.MessageUpdateRequest;
import com.BugJava.EduConnect.chat.service.ChatMessageService;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import jakarta.validation.Valid; // Valid 어노테이션 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController // REST API와 WebSocket 메시지 처리를 하나의 컨트롤러에서 담당
@RequiredArgsConstructor
@RequestMapping("/api/chat/messages") // 메시지 수정/삭제는 메시지 자체에 대한 작업이므로 별도의 경로 사용
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // 과거 채팅 메시지 조회를 위한 REST API
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long sessionId,   // 조회 대상 채팅 세션 ID
            @RequestParam(required = false) Long beforeId,  // 이전 메시지 ID (페이징 용도)
            @RequestParam(defaultValue = "10") int size) {  // 조회할 메시지 개수 (기본 10개)

        List<MessageResponse> messages = chatMessageService.findLatestMessages(sessionId, beforeId, size);
        return ResponseEntity.ok(ApiResponse.success(messages, "채팅 메시지 목록 조회에 성공했습니다."));
    }

    // 메시지 전송을 위한 WebSocket 엔드포인트
    // 사용자가 채팅방에 메시지를 보내면 이 메서드가 호출됨
    @MessageMapping("/class/{sessionId}/send") // 클라이언트가 /app/class/{sessionId}/send 로 메시지를 전송
    public void sendMessage(
            @DestinationVariable Long sessionId, // 메시지가 전송된 채팅 세션 ID
            @Valid @Payload MessageRequest messageRequest, // 클라이언트가 보낸 메시지 내용
            Principal principal // 현재 인증된 사용자 정보
    ) {
        // Principal에서 직접 userId 추출
        Long userId = Long.parseLong(principal.getName());

        // 메시지를 DB에 저장하고 MessageResponse 객체 생성
        MessageResponse sentMessage = chatMessageService.saveAndSendMessage(sessionId, userId, messageRequest);

        // /topic/class/{sessionId} 를 구독 중인 클라이언트에게 메시지를 브로드캐스트하여 실시간 전달
        messagingTemplate.convertAndSend("/topic/class/" + sessionId, sentMessage);
    }

    // 메시지 수정을 위한 REST API
    @PatchMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> updateMessage(
            @PathVariable Long messageId, // 수정 대상 메시지 ID
            @Valid @RequestBody MessageUpdateRequest updateRequest, // 수정할 내용
            @AuthenticationPrincipal Long userId // 현재 로그인한 사용자 ID
    ) {
        // 서비스에서 메시지 수정 로직 실행
        MessageResponse updatedMessage = chatMessageService.updateMessage(messageId, userId, updateRequest);

        // 수정된 메시지를 실시간으로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/class/" + updatedMessage.getSessionId(), updatedMessage);
        return ResponseEntity.ok(ApiResponse.success(updatedMessage, "메시지가 성공적으로 수정되었습니다."));
    }

    // 메시지 삭제를 위한 REST API
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteMessage(
            @PathVariable Long messageId, // 삭제 대상 메시지 ID
            @AuthenticationPrincipal Long userId // 현재 로그인한 사용자 ID
    ) {
        // 서비스에서 메시지 삭제 처리
        MessageResponse deletedMessage = chatMessageService.deleteMessage(messageId, userId);

        // 삭제된 메시지를 실시간으로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/class/" + deletedMessage.getSessionId(), deletedMessage);
        return ResponseEntity.ok(ApiResponse.success(null, "메시지가 성공적으로 삭제되었습니다."));
    }
}
