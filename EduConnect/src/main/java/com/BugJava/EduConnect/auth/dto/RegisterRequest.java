package com.BugJava.EduConnect.auth.dto;

import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

/**
 * @author rua
 */
// @Data 대신 필요한 어노테이션만 사용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String role = "STUDENT";
    private String track;

    // Swagger에서 숨기기 위한 어노테이션 추가
    @JsonIgnore
    public Track getTrackEnum() {
        return Track.fromString(track);
    }

    @JsonIgnore
    public Role getRoleEnum() {
        return Role.fromString(role);
    }
}
