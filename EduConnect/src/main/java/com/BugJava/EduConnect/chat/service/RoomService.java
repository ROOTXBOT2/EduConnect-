package com.BugJava.EduConnect.chat.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.chat.config.ChatProperties;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.dto.RoomRequest;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.dto.RoomListResponse;
import com.BugJava.EduConnect.chat.exception.RoomNotFoundException;
import com.BugJava.EduConnect.chat.exception.UnauthorizedRoomAccessException;
import com.BugJava.EduConnect.chat.repository.ChatSessionRepository;
import com.BugJava.EduConnect.chat.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository; // 강사 정보를 가져오기 위해 필요
    private final ChatSessionRepository chatSessionRepository;
    private final ChatProperties chatProperties;

    @Transactional
    public RoomResponse createRoom(Long instructorId, RoomRequest roomRequest) { // instructorId로 변경
        Users instructor = userRepository.findById(instructorId) // userId로 조회
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다."));

        String code = UUID.randomUUID().toString().substring(0, chatProperties.getRoomCodeLength()); // 8자리 고유 코드 생성

        Room room = Room.builder()
                .title(roomRequest.getTitle())
                .instructor(instructor)
                .code(code)
                .build();

        Room savedRoom = roomRepository.save(room);
        return RoomResponse.builder()
                .roomId(savedRoom.getId())
                .title(savedRoom.getTitle())
                .code(savedRoom.getCode())
                .instructorName(savedRoom.getInstructor().getName())
                .createdAt(savedRoom.getCreatedAt())
                .build();
    }

    public RoomResponse findRoomByCode(String code) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(RoomNotFoundException::new);
        return RoomResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .code(room.getCode())
                .instructorName(room.getInstructor().getName())
                .createdAt(room.getCreatedAt())
                .build();
    }

    public Room findRoomEntityByCode(String code) {
        return roomRepository.findByCode(code)
                .orElseThrow(RoomNotFoundException::new);
    }

    @Transactional
    public RoomResponse updateRoom(String code, Long userId, RoomRequest roomRequest) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(RoomNotFoundException::new);

        if (!room.getInstructor().getId().equals(userId)) {
            throw new UnauthorizedRoomAccessException("강의실을 수정할 권한이 없습니다.");
        }

        room.updateTitle(roomRequest.getTitle());

        // 다른 필드 업데이트 로직 추가 가능
        Room savedRoom = roomRepository.save(room);
        return RoomResponse.builder()
                .roomId(savedRoom.getId())
                .title(savedRoom.getTitle())
                .code(savedRoom.getCode())
                .instructorName(savedRoom.getInstructor().getName())
                .createdAt(savedRoom.getCreatedAt())
                .build();
    }

     @Transactional
     public void deleteRoom(String code, Long userId) {
         Room room = roomRepository.findByCode(code)
                 .orElseThrow(RoomNotFoundException::new);

         if (!room.getInstructor().getId().equals(userId)) {
             throw new UnauthorizedRoomAccessException("강의실을 삭제할 권한이 없습니다.");
         }

         roomRepository.delete(room);
     }

    public RoomListResponse getRoomsByInstructor(Long instructorId) {
        Users instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new UserNotFoundException("해당 ID를 가진 강사를 찾을 수 없습니다."));
        List<RoomResponse> roomResponses = roomRepository.findByInstructor(instructor).stream()
                .map(room -> {
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

        return RoomListResponse.builder()
                .rooms(roomResponses)
                .sessionCloseCutoffTime(cutoffTime)
                .build();
    }
}
