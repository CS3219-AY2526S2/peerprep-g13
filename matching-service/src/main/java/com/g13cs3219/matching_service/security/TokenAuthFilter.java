package com.g13cs3219.matching_service.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.g13cs3219.matching_service.model.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads the X-User-Id, X-User-Role, X-User-Email headers injected by the
 * API gateway after it has already validated the JWT via auth_request.
 * No outbound call to user-service is made here.
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId  = request.getHeader("X-User-Id");
        String roleStr = request.getHeader("X-User-Role");
        String email   = request.getHeader("X-User-Email");

        if (userId != null && roleStr != null && email != null) {
            try {
                Role role = Role.valueOf(roleStr.toUpperCase());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Set.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );
                authentication.setDetails(Long.parseLong(userId));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                // malformed header — leave security context unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
