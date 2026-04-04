package com.czellmer1324.licenseplategame.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    // Need to get the token itself from the authentication header from the request
    public String getTokenFromHeader(HttpServletRequest request) {
       String authHeader = request.getHeader("Authorization");
       if (authHeader == null || !authHeader.startsWith("Bearer ")) {
           return null;
       } else {
           return authHeader.substring(7);
       }
    }

    // Need to be able to generate Token from user ID
    public String generateTokenFromID(int id) {
        return Jwts.builder()
                .subject(String.valueOf(id))
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expiration))
                .signWith(key())
                .compact();
    }

    public Integer getIdFromToken(String token) {
        return Integer.parseInt(Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject());
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean verifyToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException e) {
            IO.println("Something went wrong with JWT ");
        } catch (IllegalArgumentException e) {
            IO.println("JWT claims is empty");
        }
        return false;
    }
}
