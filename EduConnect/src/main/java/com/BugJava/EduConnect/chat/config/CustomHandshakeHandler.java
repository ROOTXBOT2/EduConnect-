package com.BugJava.EduConnect.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 핸드셰이크 과정에서 사용자 인증 정보를 다루는 역할
 * 핸드셰이크 단계에서 Principal(인증 사용자 정보)을 결정하는 커스텀 로직을 구현
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    /**
     * WebSocket 핸드셰이크 시점에 현재 연결하는 클라이언트의 사용자(Principal)를 결정하는 메서드
     */
    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        // Handshake 인터셉터에서 이미 인증 정보를 attributes에 넣었을 수 있음
        Object authObj = attributes.get("authentication");
        if (authObj instanceof Authentication authentication) {
            log.debug("CustomHandshakeHandler: Authentication 객체를 찾아 Principal로 반환합니다. name={}", authentication.getName());
            return (Principal) authentication;
        }

        // 핸드셰이크 단계에서는 사용자 정보를 강제로 만들지 않고,
        // ChannelInterceptor가 STOMP CONNECT 프레임 시점에 최종적으로 인증을 처리하도록 null을 반환합니다.
        log.debug("CustomHandshakeHandler: Authentication 객체가 없어 null을 반환합니다. (ChannelInterceptor에서 설정 예정)");
        return null;
    }
}