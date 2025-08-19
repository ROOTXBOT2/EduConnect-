package com.BugJava.EduConnect.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.config.ChatProperties; // New import
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Enrollment;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.EnrollmentRequest;
import com.BugJava.EduConnect.chat.dto.EnrollmentResponse;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.dto.EnrolledRoomListResponse; // New import
import com.BugJava.EduConnect.chat.exception.AlreadyEnrolledException;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import com.BugJava.EduConnect.chat.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime; // New import
import java.time.format.DateTimeFormatter; // New import
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.BugJava.EduConnect.chat.enums.SessionStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final RoomService roomService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatProperties chatProperties;

    @Transactional
    public EnrollmentResponse enrollRoom(Long userId, EnrollmentRequest enrollmentRequest) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다."));

        Room room = roomService.findRoomEntityByCode(enrollmentRequest.getRoomCode());

        if (enrollmentRepository.existsByUserAndRoom(user, room)) {
            throw new AlreadyEnrolledException();
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .room(room)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.builder()
                .enrollmentId(savedEnrollment.getId())
                .room(RoomResponse.builder()
                        .roomId(savedEnrollment.getRoom().getId())
                        .title(savedEnrollment.getRoom().getTitle())
                        .code(savedEnrollment.getRoom().getCode())
                        .instructorName(savedEnrollment.getRoom().getInstructor().getName())
                        .createdAt(savedEnrollment.getRoom().getCreatedAt())
                        .build())
                .userEmail(savedEnrollment.getUser().getEmail())
                .build();
    }

    public EnrolledRoomListResponse getEnrolledRooms(Long userId) { // userId로 변경
        Users user = userRepository.findById(userId) // userId로 조회
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다."));

        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);

        List<RoomResponse> roomResponses = enrollments.stream()
                .map(enrollment -> {
                    Room room = enrollment.getRoom();
                    Optional<ChatSession> todaySessionOptional = chatSessionRepository.findByRoomAndSessionDate(room, LocalDate.now());

                    String todaySessionStatus = "NONE";
                    Long todaySessionId = null;

                    if (todaySessionOptional.isPresent()) {
                        ChatSession session = todaySessionOptional.get();
                        todaySessionId = session.getId();
                        switch (session.getStatus()) {
                            case OPEN:
                                todaySessionStatus = "ACTIVE";
                                break;
                            case CLOSED:
                                if (LocalTime.now().isAfter(chatProperties.getCloseCutoffTime())) {
                                    todaySessionStatus = "EXPIRED";
                                } else {
                                    todaySessionStatus = "CLOSED";
                                }
                                break;
                        }
                    }

                    return RoomResponse.builder()
                            .roomId(room.getId())
                            .title(room.getTitle())
                            .code(room.getCode())
                            .instructorName(room.getInstructor().getName())
                            .createdAt(room.getCreatedAt())
                            .todaySessionId(todaySessionId)
                            .todaySessionStatus(todaySessionStatus)
                            .build();
                })
                .collect(Collectors.toList());

        String cutoffTime = chatProperties.getCloseCutoffTime().format(DateTimeFormatter.ofPattern("HH:mm"));

        return EnrolledRoomListResponse.builder()
                .rooms(roomResponses)
                .sessionCloseCutoffTime(cutoffTime)
                .build();
    }

    @Transactional
    public void unenrollRoom(Long userId, String roomCode) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다."));

        Room room = roomService.findRoomEntityByCode(roomCode);

        Enrollment enrollment = enrollmentRepository.findByUserAndRoom(user, room)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의실에 참여하고 있지 않습니다.")); // 또는 새로운 EnrollmentNotFoundException

        enrollmentRepository.delete(enrollment);
    }
}
