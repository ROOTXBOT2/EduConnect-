package com.BugJava.EduConnect.auth.dto;

import lombok.*;

/**
 * @author rua
 */

@Getter
@AllArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}
