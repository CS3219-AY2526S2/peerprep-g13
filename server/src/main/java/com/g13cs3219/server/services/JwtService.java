package com.g13cs3219.server.services;

import java.util.Date;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final String SECRET_KEY = "muc-tieu-luong-thang-2-tram-trieu-dong";

    /**
     * Generates a JWT token for the given email. The token is valid for 24 hours.
     *
     * @param email the email to include in the token's subject
     * @return a JWT token as a String
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token expires in 24 hours
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @throws AccessDeniedException if the token is invalid or expired
     */
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token);
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid or expired JWT token");
        }
    }

    /**
     * Extracts the email from the given JWT token.
     *
     * @param token the JWT token from which to extract the email
     * @return the email extracted from the token's subject
     */
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
