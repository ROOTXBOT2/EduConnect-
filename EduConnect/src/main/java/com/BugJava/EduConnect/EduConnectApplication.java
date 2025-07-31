package com.BugJava.EduConnect;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
		info = @Info(
				title = "EduConnect API Docs", // 원하는 이름!
				version = "v1.0.0",
				description = "EduConnect 백엔드 API 명세서"
		)
)

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class EduConnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduConnectApplication.class, args);
	}

}
