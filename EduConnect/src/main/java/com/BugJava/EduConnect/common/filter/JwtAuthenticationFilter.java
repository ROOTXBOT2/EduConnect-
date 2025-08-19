package com.BugJava.EduConnect.common.filter;
import com.BugJava.EduConnect.common.service.JwtTokenProvider;
import com.BugJava.EduConnect.common.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author rua
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;


    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // 1. 요청 헤더에서 JWT 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)&& !tokenBlacklistService.isBlacklisted(token)) {
            // 2. 토큰에서 사용자 정보 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // 3. 인증 객체 생성 (실무에서는 유저 정보 추가 조회도 가능)
            // 현재는 유저당 하나의 권한만 요구 하지만 나중에는 두 개 이상이 될 수 있다.
            List<SimpleGrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

            // 마지막 권한 요소는 List<GrantedAuthority>타입만을 요구한다.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // 4. SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
