package com.g13cs3219.server.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Only process CONNECT messages (initial connection)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Get userId from session attributes (set by HttpHandshakeInterceptor)
            String userId = (String) accessor.getSessionAttributes().get("userId");
            
            // Set the userId as the authenticated principal
            if (userId != null && !userId.isEmpty()) {
                accessor.setUser(new UserPrincipal(userId));
            }
        }

        return message;
    }
}