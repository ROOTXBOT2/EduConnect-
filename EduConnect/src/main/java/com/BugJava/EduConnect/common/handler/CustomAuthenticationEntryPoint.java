package com.BugJava.EduConnect.common.handler;

import com.BugJava.EduConnect.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * @author rua
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        String code;
        String message;

        // 예외 타입/메시지로 분기 (실전에서는 좀 더 세밀한 분기 가능)
        if (authException.getMessage().contains("만료")) {
            code = "TOKEN_EXPIRED";
            message = "엑세스 토큰이 만료되었습니다.";
        } else if (authException.getMessage().contains("유효하지")) {
            code = "TOKEN_INVALID";
            message = "토큰이 유효하지 않습니다.";
        } else {
            code = "TOKEN_MISSING";
            message = "인증 정보가 없습니다.";
        }

        ApiResponse<?> apiResponse = ApiResponse.error(message, code);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}
