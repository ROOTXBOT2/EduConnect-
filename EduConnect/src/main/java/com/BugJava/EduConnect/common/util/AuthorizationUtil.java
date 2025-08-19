package com.BugJava.EduConnect.common.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthorizationUtil {

    public void checkOwnerOrAdmin(Long ownerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Long)) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Long currentUserId = (Long) authentication.getPrincipal();

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

    /**
     * 작성자 본인이거나 지정된 역할 중 하나라도 보유하면 통과.
     * 예) checkOwnerOrAnyRole(ownerId, "ROLE_INSTRUCTOR", "ROLE_ADMIN")
     */
    public void checkOwnerOrAnyRole(Long ownerId, String... allowedRoles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        // principal 타입 안전 추출 (Long이 아닐 수 있는 환경 대비)
        Object principal = auth.getPrincipal();
        if (!(principal instanceof Long currentUserId)) {
            throw new AccessDeniedException("지원하지 않는 인증 주체 타입입니다.");
        }

        // 역할 보유 여부
        boolean hasAllowedRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(granted -> Arrays.stream(allowedRoles).anyMatch(granted::equals));

        // 역할이 있으면 바로 통과
        if (hasAllowedRole) return;

        // 아니면 owner와 동일해야 통과
        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("요청을 처리할 권한이 없습니다.");
        }
    }

    /** 순수 역할 체크만 필요할 때 */
    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        Set<String> target = Set.of(roles);
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(target::contains);
    }

    /** 현재 사용자 ID (Optional) */
    public Optional<Long> currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        Object principal = auth.getPrincipal();
        return (principal instanceof Long l) ? Optional.of(l) : Optional.empty();
    }

}
