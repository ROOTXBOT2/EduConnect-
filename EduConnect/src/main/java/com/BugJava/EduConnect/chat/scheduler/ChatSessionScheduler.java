package com.BugJava.EduConnect.chat.scheduler;

import com.BugJava.EduConnect.chat.config.ChatProperties;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.enums.SessionStatus;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import com.BugJava.EduConnect.chat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSessionScheduler {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatSessionService chatSessionService;
    private final ChatProperties chatProperties;

    // 매일 오후 6시 10분 0초에 실행 (10분 유예 기간)
    @Scheduled(cron = "${chat.session.close-cron}")
    @Transactional
    public void closeExpiredChatSessions() {
        log.info("스케줄러 실행: 만료된 채팅 세션 종료 시도");

        LocalDate today = LocalDate.now();
        LocalDateTime checkDateTime = LocalDateTime.of(today, chatProperties.getCloseCheckTime());

        // 오늘 날짜의 열려 있는 모든 세션을 조회
        List<ChatSession> openSessions = chatSessionRepository.findBySessionDateAndStatus(today, SessionStatus.OPEN);

        if (openSessions.isEmpty()) {
            log.info("오늘 날짜의 열려 있는 채팅 세션이 없습니다.");
            return;
        }

        for (ChatSession session : openSessions) {
            // 세션 시작 시간이 체크 시간 이전인 경우에만 종료 처리
            if (session.getCreatedAt().isBefore(checkDateTime)) {
                try {
                    chatSessionService.closeSession(session.getId());
                    log.info("채팅 세션 종료 완료: sessionId={}", session.getId());
                } catch (Exception e) {
                    log.error("채팅 세션 종료 중 오류 발생: sessionId={}, 오류: {}", session.getId(), e.getMessage());
                }
            }
        }
        log.info("스케줄러 실행 완료: 만료된 채팅 세션 종료 시도 종료");
    }
}
