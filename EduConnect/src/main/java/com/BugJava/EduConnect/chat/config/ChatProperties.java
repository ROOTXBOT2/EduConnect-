package com.BugJava.EduConnect.chat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

@Component
@ConfigurationProperties(prefix = "chat.session")
@Getter
@Setter
@Validated
public class ChatProperties {

    @NotNull
    private String closeCron;

    @NotNull
    private LocalTime closeCutoffTime;

    @NotNull
    private LocalTime closeCheckTime;

    @NotNull
    private int roomCodeLength;
}
