package com.BugJava.EduConnect.integration;

import com.BugJava.EduConnect.EduConnectApplication;
import com.BugJava.EduConnect.auth.dto.LoginRequest;
import com.BugJava.EduConnect.auth.dto.RegisterRequest;
import com.BugJava.EduConnect.auth.dto.TokenDto;
import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.auth.service.AuthService;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = EduConnectApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AuthService authService;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    protected static class UserAuthInfo {
        public String accessToken;
        public Users user;

        UserAuthInfo(String accessToken, Users user) {
            this.accessToken = accessToken;
            this.user = user;
        }
    }

    protected UserAuthInfo registerAndLoginUser(String email, String password, String name) {
        return registerAndLoginUser(email, password, name, Role.STUDENT, Track.BACKEND);
    }

    protected UserAuthInfo registerAndLoginUser(String email, String password, String name, Role role) {
        return registerAndLoginUser(email, password, name, role, Track.BACKEND);
    }

    protected UserAuthInfo registerAndLoginUser(String email, String password, String name, Role role, Track track) {
        RegisterRequest registerRequest = new RegisterRequest(email, password, name, role.name(), track.name());
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        TokenDto tokenDto = authService.login(loginRequest);

        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found after registration"));
        return new UserAuthInfo(tokenDto.getAccessToken(), user);
    }
}