package com.BugJava.EduConnect.chat.controller;

import com.BugJava.EduConnect.chat.dto.ChatSessionResponse;
import com.BugJava.EduConnect.chat.service.ChatSessionService;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms/{roomCode}/sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    /**
     * 활성화된 채팅 세션을 조회합니다.
     * 강사(INSTRUCTOR)인 경우, 활성 세션이 없으면 Optional.empty()를 반환합니다.
     * 학생(STUDENT)인 경우, 활성 세션이 없으면 예외가 발생합니다.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getActiveSession(@PathVariable String roomCode, @AuthenticationPrincipal Long userId) {
        Optional<ChatSessionResponse> sessionResponse = chatSessionService.findActiveSession(roomCode, userId);
        return sessionResponse.map(response -> ResponseEntity.ok(ApiResponse.success(response, "활성화된 채팅 세션 정보입니다.")))
                .orElseGet(() -> ResponseEntity.noContent().build()); // Return 204 No Content if no session
    }

    /**
     * 강사가 새로운 채팅 세션을 명시적으로 시작합니다.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> startSession(@PathVariable String roomCode, @AuthenticationPrincipal Long userId) {
        ChatSessionResponse sessionResponse = chatSessionService.startNewSession(roomCode, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(sessionResponse, "새로운 채팅 세션이 시작되었습니다."));
    }

    /**
     * 특정 강의실의 종료된 채팅 세션 기록 목록을 조회합니다.
     */
    @GetMapping("/archive")
    public ResponseEntity<ApiResponse<List<ChatSessionResponse>>> getArchivedSessions(@PathVariable String roomCode) {
        List<ChatSessionResponse> archivedSessions = chatSessionService.getArchivedSessions(roomCode);
        return ResponseEntity.ok(ApiResponse.success(archivedSessions, "종료된 채팅 세션 기록 목록 조회에 성공했습니다."));
    }

    /**
     * 현재 활성화된 채팅 세션을 종료합니다. (강사 전용)
     */
    @PostMapping("/{sessionId}/close")
    public ResponseEntity<ApiResponse<Void>> closeSession(@PathVariable Long sessionId) {
        chatSessionService.closeSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "채팅 세션이 성공적으로 종료되었습니다."));
    }

    /**
     * 특정 세션 ID로 세션 정보를 조회합니다. (활성/종료 여부와 관계없이)
     */
    @GetMapping("/{sessionId}") // Relative to class-level RequestMapping
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getSessionById(@PathVariable Long sessionId) {
        ChatSessionResponse sessionResponse = chatSessionService.getSessionById(sessionId);
        return ResponseEntity.ok(ApiResponse.success(sessionResponse, "세션 정보를 성공적으로 조회했습니다."));
    }
}
