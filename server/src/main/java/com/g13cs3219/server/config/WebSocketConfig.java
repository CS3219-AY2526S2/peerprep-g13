package com.g13cs3219.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authInterceptor;

    /**
     * Register the STOMP endpoint for WebSocket connections.
     *
     * Endpoint: ws://localhost:8082/ws?userId=123
     * - /ws: WebSocket endpoint
     * - userId=123: Query parameter for user identification
     * - withSockJS(): Provides fallback for browsers without native WebSocket support
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .addInterceptors(new HttpHandshakeInterceptor())  // Extract userId from handshake
                .setAllowedOriginPatterns("*")                   // Allow all origins (configure for production)
                .withSockJS();                                    // Enable SockJS fallback
    }

    /**
     * Configure the inbound message channel with authentication interceptor.
     *
     * This intercepts all incoming STOMP messages and sets the user principal
     * based on the userId extracted during handshake.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }

    /**
     * Configure the message broker for routing STOMP messages.
     *
     * Message Destinations:
     * - /topic/* : Public topics (broadcast to multiple subscribers)
     * - /queue/* : Private queues (user-specific, one-to-one messages)
     *
     * Application Prefix: /app
     * - User messages sent to /app/* are routed to @MessageMapping methods
     *
     * Message Flow:
     * - Backend: messagingTemplate.convertAndSendToUser(userId, "/queue/match", data)
     * - Frontend subscribes to: /user/queue/match (Spring adds /user prefix)
     * - Frontend receives: data from /queue/match
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory message broker for /topic and /queue destinations
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Set prefix for routes handled by @MessageMapping annotated methods
        registry.setApplicationDestinationPrefixes("/app");
    }
}
