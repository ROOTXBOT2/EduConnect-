package com.BugJava.EduConnect.chat.repository;

import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

import com.BugJava.EduConnect.chat.enums.SessionStatus;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByRoomAndSessionDate(Room room, LocalDate sessionDate);
    List<ChatSession> findByRoomAndStatusOrderBySessionDateDesc(Room room, SessionStatus status);
    List<ChatSession> findBySessionDateAndStatus(LocalDate sessionDate, SessionStatus status);
    Optional<ChatSession> findByRoomAndStatus(Room room, SessionStatus status);
    Optional<ChatSession> findByIdAndStatus(Long id, SessionStatus status);
}
