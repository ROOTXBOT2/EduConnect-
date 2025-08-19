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

@Configuration
@RequiredArgsConstructor
public class AssignmentSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    @Order(2)
    public SecurityFilterChain assignmentFilterChain(HttpSecurity http) throws Exception {
        http
                //적용 범위 지정
                .securityMatcher("/api/assignments/**")

                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.GET, "/api/assignments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/assignments/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/assignments")
                            .hasAnyRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/assignments/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/assignments/*").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/assignments/*/comments").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/assignments/*/comments/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/assignments/*/comments/*").authenticated()

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
