package com.BugJava.EduConnect.auth.service;

import com.BugJava.EduConnect.auth.dto.LoginRequest;
import com.BugJava.EduConnect.auth.dto.RegisterRequest;
import com.BugJava.EduConnect.auth.dto.TokenDto;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.exception.InvalidEmailPasswordException;
import com.BugJava.EduConnect.auth.exception.InvalidRefreshTokenException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.auth.exception.DuplicateEmailException;
import com.BugJava.EduConnect.common.service.JwtTokenProvider;
import com.BugJava.EduConnect.common.service.InMemoryTokenBlacklistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

/**
 * @author rua
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final InMemoryTokenBlacklistService blacklistService;
    private final RefreshTokenStore refreshTokenStore; // InMemory/Redis 중 하나 주입

    public Users register(RegisterRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용중인 이메일입니다.");
        }
        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Users 엔티티 생성 (빌더 패턴 + 기본값 포함)
        Users user = Users.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .role(request.getRole() != null ? request.getRoleEnum() : Role.STUDENT) // 기본값 STUDENT
                .track(request.getTrackEnum())
                .isDeleted(false)
                .deletedAt(null)
                .build();
        return userRepository.save(user);
    }

    public TokenDto login(LoginRequest request) {
        // 1. 이메일로 유저 조회
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidEmailPasswordException("Invalid email or password"));

        // 2. 비밀번호 일치 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidEmailPasswordException("Invalid email or password");
        }

        // 3. 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());
        // RefreshToken 만료일 추출
        Date expiry = jwtTokenProvider.getExpiryFromToken(refreshToken);
        long expiresAt = expiry.getTime();
        // 4. (선택) RefreshToken을 DB/Redis에 저장
        refreshTokenStore.save(user.getId(), refreshToken, expiresAt);
        return new TokenDto(accessToken, refreshToken);
    }

    public void logout(TokenDto token) {
        // 1. RefreshToken 삭제
        Long userId = jwtTokenProvider.getUserIdFromToken(token.getRefreshToken());
        refreshTokenStore.delete(userId);

        // 2. AccessToken 만료 시간 추출
        String accessToken = token.getAccessToken();
        Date expiry = jwtTokenProvider.getExpiryFromToken(accessToken);

        // 3. 블랙리스트에 저장 (만료 시점까지)
        blacklistService.addToBlacklist(accessToken, expiry.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
    }

    // Refresh 토큰 재발급
    public TokenDto refreshToken(TokenDto tokendto) {
        // 리프레쉬 토큰만 필요, 해더에 받아 진행시키기.
        String refreshToken = tokendto.getRefreshToken();
        // 1. 토큰 기본 유효성 검증 (서명, exp 등)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. 저장소에서 해당 유저의 토큰 가져오기 (만료 체크는 내부 get에서!)
        String storedToken = refreshTokenStore.get(userId);

        if (storedToken == null) {
            throw new InvalidRefreshTokenException("리프레시 토큰이 존재하지 않습니다. (만료/로그아웃 등)");
        }

        // 4. 저장소(메모리/Redis)에서 토큰 일치 여부 체크
        if (!refreshToken.equals(storedToken)) {
            throw new InvalidRefreshTokenException("리프레시 토큰이 일치하지 않습니다.");
        }

        // 5. 유저 정보 조회 (역할 등)
        Users user = userRepository.findById(userId).orElseThrow(() -> new InvalidRefreshTokenException("해당 토큰 사용자는 없습니다."));

        // 6. 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, user.getRole());

        // 7. 기존 토큰 삭제
        refreshTokenStore.delete(userId);

        // 8. 새로운 리프레시 토큰 저장(갱신)
        refreshTokenStore.save(userId, newRefreshToken, jwtTokenProvider.getExpiryFromToken(newRefreshToken).getTime());
        return new TokenDto(newAccessToken, newRefreshToken);
    }
}