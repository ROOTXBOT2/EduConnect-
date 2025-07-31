package com.BugJava.EduConnect.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rua
 */
@Component
@Slf4j
public class InMemoryTokenBlacklistService implements TokenBlacklistService {
    // 추후 Redis로 교체 가능
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    private final Map<String, LocalDateTime> expiryMap = new ConcurrentHashMap<>();

    @Override
    public void addToBlacklist(String token, LocalDateTime expiry) {
        blacklist.add(token);
        expiryMap.put(token, expiry);
        log.info("Token added to blacklist: {}", token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        // 만료된 토큰은 자동 정리됨(아래 @Scheduled 참고)
        return blacklist.contains(token);
    }

    @Override
    @Scheduled(fixedDelay = 60_000) // 1분마다 만료된 토큰 자동 정리 (운영환경엔 더 짧거나 길게 조정 가능)
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int removedCount = 0;
        for (Map.Entry<String, LocalDateTime> entry : expiryMap.entrySet()) {
            if (entry.getValue().isBefore(now)) {
                blacklist.remove(entry.getKey());
                removedCount++;
            }
        }
        expiryMap.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        if (removedCount > 0) {
            log.info("Expired tokens cleaned up: {}", removedCount);
        }
    }
}