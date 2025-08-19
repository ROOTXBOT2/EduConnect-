package com.BugJava.EduConnect.chat.controller;

import com.BugJava.EduConnect.chat.dto.RoomRequest;
import com.BugJava.EduConnect.chat.dto.RoomResponse;
import com.BugJava.EduConnect.chat.dto.RoomListResponse; // New import
import com.BugJava.EduConnect.chat.service.RoomService;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@AuthenticationPrincipal Long userId, @Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse roomResponse = roomService.createRoom(userId, roomRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(roomResponse, "채팅방이 성공적으로 생성되었습니다."));
    }

    // 강사 : 강의 = 1 : 1 수정
    @GetMapping
    public ResponseEntity<ApiResponse<RoomListResponse>> getRooms(@AuthenticationPrincipal Long userId) {
        RoomListResponse roomListResponse = roomService.getRoomsByInstructor(userId);
        return ResponseEntity.ok(ApiResponse.success(roomListResponse, "강의실 목록 조회에 성공했습니다."));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable String code) {
        RoomResponse roomResponseDto = roomService.findRoomByCode(code);
        return ResponseEntity.ok(ApiResponse.success(roomResponseDto, "채팅방 조회에 성공했습니다."));
    }

    @PatchMapping("/{code}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(@PathVariable String code, @AuthenticationPrincipal Long userId, @Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse updatedRoom = roomService.updateRoom(code, userId, roomRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedRoom, "채팅방이 성공적으로 업데이트되었습니다."));
    }

     @DeleteMapping("/{code}")
     public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable String code, @AuthenticationPrincipal Long userId) {
         roomService.deleteRoom(code, userId);
         return ResponseEntity.ok(ApiResponse.success(null, "채팅방이 성공적으로 삭제되었습니다."));
     }
}
