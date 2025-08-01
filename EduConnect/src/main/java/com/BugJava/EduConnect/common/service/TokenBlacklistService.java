package com.BugJava.EduConnect.common.service;

import java.time.LocalDateTime;

/**
 * @author rua
 */
public interface TokenBlacklistService {
    void addToBlacklist(String token, LocalDateTime expiry);
    boolean isBlacklisted(String token);
    void cleanupExpiredTokens();
}