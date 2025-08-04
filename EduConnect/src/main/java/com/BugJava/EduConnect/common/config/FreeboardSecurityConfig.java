package com.BugJava.EduConnect.common.config;

import com.BugJava.EduConnect.common.filter.JwtAuthenticationFilter;
import com.BugJava.EduConnect.common.handler.CustomAccessDeniedHandler;
import com.BugJava.EduConnect.common.handler.CustomAuthenticationEntryPoint;
import com.BugJava.EduConnect.common.service.JwtTokenProvider;
import com.BugJava.EduConnect.common.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class FreeboardSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final TokenBlacklistService tokenBlacklistService;

    @Bean
    @Order(1)
    public SecurityFilterChain freeboardFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/posts/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 게시글 권한 설정 (*는 postId)
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll() // 전체 목록 조회: 누구나
                        .requestMatchers(HttpMethod.GET, "/api/posts/*").authenticated() // 상세 조회: 로그인 필수
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated() // 생성: 로그인 필수
                        .requestMatchers(HttpMethod.PUT, "/api/posts/*").authenticated() // 수정: 로그인 필수
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*").authenticated() // 삭제: 로그인 필수

                        // 댓글 권한 설정
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/comments").authenticated() // 생성: 로그인 필수
                        .requestMatchers(HttpMethod.PUT, "/api/posts/*/comments/*").authenticated() // 수정: 로그인 필수
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*/comments/*").authenticated() // 삭제: 로그인 필수

                        // 그 외 /api/posts/** 패턴의 모든 요청은 일단 허용 (이미 위에서 다 처리됨)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }
}
