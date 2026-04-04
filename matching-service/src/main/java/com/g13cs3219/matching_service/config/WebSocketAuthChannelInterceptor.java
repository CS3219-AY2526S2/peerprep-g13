package com.g13cs3219.matching_service.config;

import java.security.Principal;
import java.util.Map;

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

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
            String userId = null;

            if (sessionAttrs != null) {
                userId = (String) sessionAttrs.get("userId");
            }

            if (userId == null) {
                // fallback from handshake Principal
                Principal p = accessor.getUser();
                if (p != null) userId = p.getName();
            }

            if (userId != null && !userId.isEmpty()) {
                accessor.setUser(new UserPrincipal(userId));
            }
        }

        return message;
    }
}