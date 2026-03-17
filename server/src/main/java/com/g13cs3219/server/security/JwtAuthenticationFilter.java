package com.g13cs3219.server.security;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.g13cs3219.server.model.User;
import com.g13cs3219.server.services.JwtService;
import com.g13cs3219.server.services.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // If the Authorization header is missing or does not start with "Bearer ",
        // skip authentication and continue the filter chain
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token from the Authorization header and validate it
        String token = authHeader.substring(7);
        try {
            jwtService.validateToken(token);
        } catch(Exception e) {
            handleAuthenticationException(response, e);
            return;
        }

        // Extract the email from the token and check if the user exists
        String email = jwtService.extractEmail(token);
        User user = userService.getUserByEmail(email);

        // If the user exists, set the authentication in the security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Handles authentication exceptions by sending a 401 Unauthorized response with a JSON error message.
     *
     * @param response the HttpServletResponse to send the error response
     * @param e the exception that occurred during authentication
     * @throws IOException if an input or output exception occurs while writing the response
     */
    private void handleAuthenticationException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> errorBody = Map.of("error", e.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
