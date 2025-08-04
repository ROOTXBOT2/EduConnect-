package com.BugJava.EduConnect.common.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtil {

    public void checkOwnerOrAdmin(Long ownerId, Long currentUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            return; // 관리자는 모든 권한을 가짐
        }

        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("요청을 처리할 권한이 없습니다.");
        }
    }
}
