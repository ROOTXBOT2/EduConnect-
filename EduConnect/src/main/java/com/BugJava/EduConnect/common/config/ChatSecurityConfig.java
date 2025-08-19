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
public class ChatSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource; // CorsConfigurationSource 주입

    @Bean
    @Order(2)
    public SecurityFilterChain chatFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 추가
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .securityMatcher("/api/chat/**", "/ws-stomp/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS 메서드 허용
                        .requestMatchers(HttpMethod.GET, "/ws-stomp/**").permitAll() // SockJS info 및 기타 GET 요청 허용
                        .requestMatchers("/ws-stomp/**").authenticated() // WebSocket 연결 허용

                        // RoomController
                        .requestMatchers(HttpMethod.POST, "/api/chat/rooms").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/chat/rooms").hasRole("INSTRUCTOR") // For getRooms (instructor's own rooms)
                        .requestMatchers(HttpMethod.GET, "/api/chat/rooms/{code}").authenticated() // For getRoom by code
                        .requestMatchers(HttpMethod.PATCH, "/api/chat/rooms/*").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/chat/rooms/*").hasRole("INSTRUCTOR")

                        // ChatMessageController
                        .requestMatchers(HttpMethod.GET, "/api/chat/messages/sessions/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/chat/messages/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/chat/messages/*").authenticated()

                        // ChatSessionController
                        .requestMatchers(HttpMethod.GET, "/api/chat/rooms/*/sessions/*").authenticated() // Rule for specific session by ID within a room
                        .requestMatchers(HttpMethod.GET, "/api/chat/rooms/*/sessions/active").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/chat/rooms/*/sessions/archive").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/chat/rooms/*/sessions/start").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/chat/rooms/*/sessions/*/close").hasRole("INSTRUCTOR")

                        // EnrollmentController
                        .requestMatchers(HttpMethod.POST, "/api/chat/enrollments").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/chat/enrollments/my-rooms").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/chat/enrollments/rooms/*").hasRole("STUDENT")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }
}
