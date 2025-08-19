package com.BugJava.EduConnect.common.service;

import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track; // Track Enum 임포트 추가
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author rua
 */
@Service
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private SecretKey jwtSecretKey; // 실제 암호화용 Key 객체

    // 시크릿 키를 SecretKey 객체로 변환 (JJWT 0.12.x 필수)
    @PostConstruct
    public void init() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 1. AccessToken 생성 (신규 Builder API)
    public String createAccessToken(Long userId, String name, Role role, Track track, String email) { // name, track, email 파라미터 추가
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .subject(String.valueOf(userId))    // (구) setSubject() → (신) subject()
                .claim("name", name)     // name 클레임 추가
                .claim("role", role)
                .claim("track", track)   // track 클레임 추가
                .claim("email", email)   // email 클레임 추가
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(jwtSecretKey)                      // SignatureAlgorithm 생략, key로 감지
                .compact();
    }
        // 2. RefreshToken 생성 (신규 Builder API)
        public String createRefreshToken(Long userId, Role role) {
            final Date now = new Date();
            final Date expiry = new Date(now.getTime() + refreshTokenValidity);

            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .claim("role", role)
                    .issuedAt(now)
                    .expiration(expiry)
                    .signWith(jwtSecretKey)
                    .compact();
        }

    // 3. 토큰 유효성 검증 (신규 Parser API)
    /**
     * JWT 토큰의 유효성을 검증한다.
     * - JJWT의 parseSignedClaims() 호출 시
     *   - 토큰 시그니처(서명), exp(만료), nbf(사용시작) 등 모든 표준 클레임을 자동 검증
     *   - 만료(expired)시 ExpiredJwtException, 그 외 서명/포맷 오류시 JwtException 발생
     * - 유효할 경우 true, 만료/오류시 false 반환
     */
    public boolean validateToken(String token) {

        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey) // 키 세팅 (시그니처 검증)
                    .build()
                    .parseSignedClaims(token);  // [★] 이 한 줄에서 모든 표준 클레임, 시그니처 등 검증됨!
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // 4. Claims 추출 - 공통 파서
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 5. 사용자 ID(Subject) 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    // 6. Role(권한) 추출
    public String getRoleFromToken(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // 7. expiration(만료일) 추출
    public Date getExpiryFromToken(String token) {
        return extractAllClaims(token).getExpiration();
    }
}