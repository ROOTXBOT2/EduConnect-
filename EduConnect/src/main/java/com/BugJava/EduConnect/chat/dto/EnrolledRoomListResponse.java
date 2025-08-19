package com.BugJava.EduConnect.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EnrolledRoomListResponse {
    private List<RoomResponse> rooms;
    private String sessionCloseCutoffTime;
}
