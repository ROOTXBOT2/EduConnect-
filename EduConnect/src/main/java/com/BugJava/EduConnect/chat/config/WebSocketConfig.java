package com.BugJava.EduConnect.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * STOMP 기반 웹소켓 메시징 활성화
 * -> 클라이언트와 실시간 메시지를 주고받는 기반 준비
 */
@Configuration
@EnableWebSocketMessageBroker // 웹소켓 + 메시지 브로커(메시지 전달을 중계하는 역할) 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final CustomHandshakeHandler customHandshakeHandler;
    private final JwtChannelInterceptor jwtChannelInterceptor;

    /**
     * 클라이언트가 WebSocket 연결을 시작할 때 접속하는 엔드포인트를 등록하는 메서드
     * /ws-stomp 엔드포인트에 웹소켓 연결 요청
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .addInterceptors(jwtHandshakeInterceptor) // WebSocket 핸드셰이크 시 JWT 인증을 1차적으로 수행
                .setHandshakeHandler(customHandshakeHandler) // 커스텀 핸드셰이크 동작을 지정
                // 운영 시에는 "*" 대신 실제 프론트 도메인 리스트로 제한.
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // WebSocket을 지원하지 않는 환경에 대비해 SockJS 폴백(fallback) 기능을 활성화
    }

    /**
     * 메시지 브로커(중계)를 설정하고, [클라이언트 → 서버], [서버 → 클라이언트] 메시지의 경로를 정의
     * /app 경로로 들어오는 메시지는 @MessageMapping으로 라우팅
     * /topic 경로는 구독 메시지 브로드캐스트용
     * -> /app은 서버로 요청 보내는 용도, /topic은 서버에서 클라이언트로 실시간 메시지 보내는 용도
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // [클라이언트 → 서버] 메시지 prefix
        registry.enableSimpleBroker("/topic"); // [서버 → 클라이언트] 메시지 브로커 경로
    }

    /**
     * 클라이언트가 서버로 보내는 모든 메시지 전송 과정에 인터셉터를 등록
     * jwtChannelInterceptor를 등록하여 STOMP CONNECT 프레임 등 모든 클라이언트 인바운드 메시지를 가로채 JWT 인증을 수행
     * WebSocket 연결 이후 단계에서 최종적으로 사용자를 인증하고, 인증 정보를 Principal로 등록
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    /**
     * WebSocket 메시지의 데이터 변환 방식을 설정
     * Jackson 컨버터를 추가하여, JSON 형태의 메시지를 객체로, 객체를 JSON으로 자동 변환
     * return false => 기본 컨버터들도 그대로 유지하도록 하여 기존 프레임워크 동작에 영향을 주지 않기 위함 (프레임워크의 기본 컨버터들이 뒤에 추가됨)
     * 따라서 JSON 메시지를 주고받을 때 별도의 변환 코드를 작성할 필요 없이, 객체 기반으로 메시지 처리가 가능
     */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        messageConverters.add(new MappingJackson2MessageConverter());
        return false;
    }
}
