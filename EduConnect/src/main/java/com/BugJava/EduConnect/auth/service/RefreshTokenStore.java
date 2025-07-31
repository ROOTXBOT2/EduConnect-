package com.BugJava.EduConnect.auth.service;

/**
 * @author rua
 */
public interface RefreshTokenStore {
    void save(Long userId, String refreshToken, long expiresAt);
    String get(Long userId);
    void delete(Long userId);
}
