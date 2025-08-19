package com.BugJava.EduConnect.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author rua
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\"권한이 없습니다.\",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }
}