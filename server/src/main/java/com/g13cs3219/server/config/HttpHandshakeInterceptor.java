package com.g13cs3219.server.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * Called before the WebSocket handshake is performed.
     * Extracts userId from query parameters and stores it in session attributes.
     *
     * @param request the ServerHttpRequest (servlet-based HTTP request)
     * @param response the ServerHttpResponse
     * @param wsHandler the WebSocketHandler
     * @param attributes the attributes map to store session data
     * @return true to proceed with handshake, false to reject
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String userId = servletRequest.getServletRequest().getParameter("userId");
            if (userId != null && !userId.isEmpty()) {
                attributes.put("userId", userId);
            }
        }
        return true;
    }

    /**
     * Called after the WebSocket handshake is performed.
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // No-op: cleanup or logging can be added here if needed
    }
}
