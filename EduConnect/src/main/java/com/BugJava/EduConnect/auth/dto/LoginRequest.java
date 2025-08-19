package com.BugJava.EduConnect.auth.dto;

import lombok.Data;

/**
 * @author rua
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
}
