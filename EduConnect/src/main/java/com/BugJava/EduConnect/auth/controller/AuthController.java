package com.BugJava.EduConnect.auth.controller;

import com.BugJava.EduConnect.auth.dto.LoginRequest;
import com.BugJava.EduConnect.auth.dto.RegisterRequest;
import com.BugJava.EduConnect.auth.dto.TokenDto;
import com.BugJava.EduConnect.auth.service.AuthService;
import com.BugJava.EduConnect.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author rua
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request); // 성공 시만 진행, 실패 시 예외 던짐 → 자동 처리
        return ResponseEntity.ok(ApiResponse.success(null, "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> login(@Valid @RequestBody LoginRequest request) {
        TokenDto token = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(token, "로그인이 완료되었습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@Valid @RequestBody TokenDto tokendto) {
        authService.logout(tokendto);
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃이 완료되었습니다."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refresh(@Valid @RequestBody TokenDto tokendto) {
        TokenDto newTokens = authService.refreshToken(tokendto);
        return ResponseEntity.ok(ApiResponse.success(newTokens, "토큰이 재발급되었습니다."));

    }
}