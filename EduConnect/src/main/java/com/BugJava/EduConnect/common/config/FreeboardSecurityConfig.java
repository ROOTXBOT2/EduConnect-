package com.BugJava.EduConnect.common.config;

import com.BugJava.EduConnect.common.filter.JwtAuthenticationFilter;
import com.BugJava.EduConnect.common.handler.CustomAccessDeniedHandler;
import com.BugJava.EduConnect.common.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class FreeboardSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    @Order(1) // 우선 순위 설정 (default가 먼저 실행되어서 보안 정책이 적용되지 않는 문제 해결)
    public SecurityFilterChain freeboardFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 추가
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 적용 범위 지정 (/api/posts/**)
                .securityMatcher("/api/posts/**")

                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CORS 설정 추가
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS 메서드는 인증 없이 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 게시글 권한 설정 (*는 postId)
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll() // 전체 목록 조회: 누구나
                        .requestMatchers(HttpMethod.GET, "/api/posts/*").authenticated() // 상세 조회: 로그인 필수
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated() // 생성: 로그인 필수
                        .requestMatchers(HttpMethod.PUT, "/api/posts/*").authenticated() // 수정: 로그인 필수
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*").authenticated() // 삭제: 로그인 필수

                        // 댓글 권한 설정
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").authenticated() // 댓글 목록 조회
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/comments").authenticated() // 생성: 로그인 필수
                        .requestMatchers(HttpMethod.PUT, "/api/posts/*/comments/*").authenticated() // 수정: 로그인 필수
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*/comments/*").authenticated() // 삭제: 로그인 필수

                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }
}