package com.BugJava.EduConnect.chat.controller;

import com.BugJava.EduConnect.chat.dto.EnrollmentRequest;
import com.BugJava.EduConnect.chat.dto.EnrollmentResponse;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.dto.EnrolledRoomListResponse; // New import
import com.BugJava.EduConnect.chat.service.EnrollmentService;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollRoom(@AuthenticationPrincipal Long userId, @RequestBody EnrollmentRequest enrollmentRequest) {
        EnrollmentResponse enrollmentResponse = enrollmentService.enrollRoom(userId, enrollmentRequest);
        return ResponseEntity.ok(ApiResponse.success(enrollmentResponse, "강의실 참여에 성공했습니다."));
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<ApiResponse<EnrolledRoomListResponse>> getMyEnrolledRooms(@AuthenticationPrincipal Long userId) {
        EnrolledRoomListResponse enrolledRooms = enrollmentService.getEnrolledRooms(userId);
        return ResponseEntity.ok(ApiResponse.success(enrolledRooms, "참여 중인 강의실 목록 조회에 성공했습니다."));
    }

    @DeleteMapping("/rooms/{roomCode}")
    public ResponseEntity<ApiResponse<Void>> unenrollRoom(@AuthenticationPrincipal Long userId, @PathVariable String roomCode) {
        enrollmentService.unenrollRoom(userId, roomCode);
        return ResponseEntity.ok(ApiResponse.success(null, "강의실 참여가 성공적으로 취소되었습니다."));
    }
}
