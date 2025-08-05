package com.BugJava.EduConnect.auth.dto;

import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

/**
 * @author rua
 */
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

    @JsonIgnore
    public Track getTrackEnum() {
        return Track.fromString(track);
    }

    @JsonIgnore
    public Role getRoleEnum() {
        return Role.fromString(role);
    }
}
