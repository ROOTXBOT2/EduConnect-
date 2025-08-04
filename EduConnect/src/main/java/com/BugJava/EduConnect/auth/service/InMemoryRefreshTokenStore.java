package com.BugJava.EduConnect.auth.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rua
 */
@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final ConcurrentHashMap<Long, TokenInfo> store = new ConcurrentHashMap<>();

    // 1. 토큰 정보(값+만료)를 담는 내부 클래스(혹은 별도 파일)
    private static class TokenInfo {
        final String token;
        final long expiresAt;
        TokenInfo(String token, long expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }
    }

    @Override
    public void save(Long userId, String refreshToken, long expiresAt) {
        store.put(userId, new TokenInfo(refreshToken, expiresAt));
    }

    @Override
    public String get(Long userId) {
        TokenInfo info = store.get(userId);
        if (info == null) return null;
        if (System.currentTimeMillis() > info.expiresAt) {
            store.remove(userId); // 만료시 자동 삭제
            return null;
        }
        return info.token;
    }

    @Override
    public void delete(Long userId) {
        // 비인가 접근을 통한 Logout 요청시 어떻게 반환하는지.
        store.remove(userId);
    }
}
