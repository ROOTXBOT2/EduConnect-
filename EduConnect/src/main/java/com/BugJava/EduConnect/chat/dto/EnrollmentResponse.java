package com.BugJava.EduConnect.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EnrollmentResponse {
    private Long enrollmentId;
    private RoomResponse room;
    private String userEmail;

}
