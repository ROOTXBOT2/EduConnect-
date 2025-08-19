package com.BugJava.EduConnect.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api/ 로 시작하는 모든 요청에 대해
                .allowedOrigins("http://localhost:5173") // React 앱의 주소만 허용
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT") // 허용할 HTTP 메서드
                .allowCredentials(true);
    }
}