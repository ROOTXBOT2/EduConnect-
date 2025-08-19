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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * @author rua
 */

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class QnASecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    @Order(3) // 우선 순위 설정 (default가 먼저 실행되어서 보안 정책이 적용되지 않는 문제 해결)
    public SecurityFilterChain qnaboardFilterChain(HttpSecurity http) throws Exception {
        http
                // 적용 범위 지정 (/api/qna/**)
                .securityMatcher("/api/qna/**")

                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CORS 설정 추가
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        // QnA 권한 설정 
                        .requestMatchers(HttpMethod.GET, "/api/qna/questions").authenticated() // 목록 조회: 로그인 필수
                        .requestMatchers(HttpMethod.POST, "/api/qna/questions/search").authenticated() // 검색: 로그인 필수
                        .requestMatchers(HttpMethod.GET, "/api/qna/questions/*").authenticated() // 상세 조회: 로그인 필수
                        .requestMatchers(HttpMethod.POST, "/api/qna/questions").authenticated() // 생성: 로그인 필수
                        .requestMatchers(HttpMethod.PUT, "/api/qna/questions/*").authenticated() // 수정: 로그인 필수
                        .requestMatchers(HttpMethod.DELETE, "/api/qna/questions/*").authenticated() // 삭제: 로그인 필수
                        
                        // Answer와 Comment 관련 API도 인증 필요
                        .requestMatchers("/api/qna/questions/*/answers/**").authenticated()
                        .requestMatchers("/api/qna/questions/*/comments/**").authenticated()
                        
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
