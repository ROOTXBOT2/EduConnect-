package com.BugJava.EduConnect.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.config.ChatProperties;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.ChatSessionResponse;
import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.chat.exception.ChatSessionNotFoundException;
import com.BugJava.EduConnect.chat.exception.SessionNotStartedException;
import com.BugJava.EduConnect.chat.exception.UnauthorizedRoomAccessException;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final RoomService roomService; // Room 정보를 가져오기 위해 의존
    private final UserRepository userRepository;
    private final ChatProperties chatProperties;

    /**
     * =================================================================================
     * Public - Command Methods (세션 상태 변경)
     * =================================================================================
     */

    @Transactional
    public ChatSessionResponse startNewSession(String roomCode, Long instructorId) {
        Users instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 강사를 찾을 수 없습니다."));

        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new UnauthorizedRoomAccessException("강사만 세션을 시작할 수 있습니다.");
        }

        Room room = roomService.findRoomEntityByCode(roomCode);
        LocalDate today = LocalDate.now();

        // 오늘 날짜의 세션이 이미 존재하는지 확인합니다.
        Optional<ChatSession> todaySessionOptional = chatSessionRepository.findByRoomAndSessionDate(room, today);

        if (todaySessionOptional.isPresent()) {
            ChatSession todaySession = todaySessionOptional.get();
            // 오늘의 세션이 이미 활성화 상태라면 예외를 발생시킵니다.
            if (todaySession.getStatus() == SessionStatus.OPEN) {
                throw new IllegalStateException("이미 오늘 활성화된 세션이 존재합니다.");
            }

            // 오늘 세션이 종료된 경우, 재시작 가능 시간을 확인합니다.
            if (LocalTime.now().isAfter(chatProperties.getCloseCutoffTime())) {
                throw new IllegalStateException("자동 세션 종료 시간(" + chatProperties.getCloseCutoffTime() + ")이 지나 세션을 이어할 수 없습니다.");
            }

            todaySession.reopen();
            ChatSession savedSession = chatSessionRepository.save(todaySession);

            return ChatSessionResponse.builder()
                    .sessionId(savedSession.getId())
                    .title(savedSession.getTitle())
                    .sessionDate(savedSession.getSessionDate())
                    .active(savedSession.getStatus() == SessionStatus.OPEN)
                    .startTime(savedSession.getCreatedAt())
                    .endTime(savedSession.getEndTime())
                    .build();
        } else {
            // 오늘 날짜의 세션이 없는 경우, 새로운 세션을 생성합니다.
            return createNewSession(room);
        }
    }

    @Transactional
    public void closeSession(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(ChatSessionNotFoundException::new);
        session.close();
        chatSessionRepository.save(session);
    }

    /**
     * =================================================================================
     * Public - Query Methods (세션 정보 조회)
     * =================================================================================
     */

    public Optional<ChatSessionResponse> findActiveSession(String roomCode, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다."));

        Room room = roomService.findRoomEntityByCode(roomCode);

        // 해당 방에 활성화된 세션이 있는지 날짜와 관계없이 조회합니다.
        Optional<ChatSession> activeSessionOptional = chatSessionRepository.findByRoomAndStatus(room, SessionStatus.OPEN);

        if (activeSessionOptional.isPresent()) {
            ChatSession session = activeSessionOptional.get();
            return Optional.of(ChatSessionResponse.builder()
                    .sessionId(session.getId())
                    .title(session.getTitle())
                    .sessionDate(session.getSessionDate())
                    .active(session.getStatus() == SessionStatus.OPEN)
                    .startTime(session.getCreatedAt())
                    .endTime(session.getEndTime())
                    .build());
        } else {
            // 활성화된 세션이 없는 경우
            if (user.getRole() == Role.STUDENT) {
                // 학생은 활성 세션이 없으면 접근할 수 없습니다.
                throw new SessionNotStartedException();
            }
            // 강사는 활성 세션이 없는 경우 새 세션을 시작할 수 있습니다.
            return Optional.empty();
        }
    }

    public List<ChatSessionResponse> getArchivedSessions(String roomCode) {
        Room room = roomService.findRoomEntityByCode(roomCode);
        return chatSessionRepository.findByRoomAndStatusOrderBySessionDateDesc(room, SessionStatus.CLOSED).stream()
                .map(session -> ChatSessionResponse.builder()
                        .sessionId(session.getId())
                        .title(session.getTitle())
                        .sessionDate(session.getSessionDate())
                        .active(session.getStatus() == SessionStatus.OPEN)
                        .startTime(session.getCreatedAt())
                        .endTime(session.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    public ChatSessionResponse getSessionById(Long sessionId) {
        ChatSession session = chatSessionRepository.findByIdAndStatus(sessionId, SessionStatus.CLOSED)
                .orElseThrow(ChatSessionNotFoundException::new);
        return ChatSessionResponse.builder()
                .sessionId(session.getId())
                .title(session.getTitle())
                .sessionDate(session.getSessionDate())
                .active(session.getStatus() == SessionStatus.OPEN)
                .startTime(session.getCreatedAt())
                .endTime(session.getEndTime())
                .build();
    }

    public ChatSession findSessionEntity(Long sessionId) {
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(ChatSessionNotFoundException::new);
    }

    /**
     * =================================================================================
     * Private - Helper Methods
     * =================================================================================
     */

    private ChatSessionResponse createNewSession(Room room) {
        String title = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 수업 기록"));
        ChatSession newSession = ChatSession.builder()
                .room(room)
                .title(title)
                .build();
        ChatSession savedSession = chatSessionRepository.save(newSession);
        return ChatSessionResponse.builder()
                .sessionId(savedSession.getId())
                .title(savedSession.getTitle())
                .sessionDate(savedSession.getSessionDate())
                .active(savedSession.getStatus() == SessionStatus.OPEN)
                .startTime(savedSession.getCreatedAt())
                .endTime(savedSession.getEndTime())
                .build();
    }
}