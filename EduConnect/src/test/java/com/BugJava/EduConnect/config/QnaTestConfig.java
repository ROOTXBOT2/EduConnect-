package com.BugJava.EduConnect.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * QnA 테스트를 위한 설정 클래스
 * 
 * @author rua
 */
@TestConfiguration
@Profile("test")
@TestPropertySource(properties = {
    "jwt.secret-key=testSecretKeyForJwtTokenGenerationMinimum32Characters",
    "jwt.access-token-validity=3600000",
    "jwt.refresh-token-validity=86400000",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.show-sql=true",
    "logging.level.org.springframework.security=DEBUG"
})
public class QnaTestConfig {

    @Bean
    @Primary
    @Profile("test")
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder(4); // 테스트용으로 낮은 강도 사용
    }
}