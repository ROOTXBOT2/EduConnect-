package com.BugJava.EduConnect.auth.dto;

import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import lombok.Data;

/**
 * @author rua
 */
@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String role = "STUDENT";
    private String track;

    public Track getTrackEnum() {
        return Track.fromString(track);
    }
    public Role getRoleEnum() {
        return Role.fromString(role);
    }
}
