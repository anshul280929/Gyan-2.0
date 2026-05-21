package com.gyan.security;

import java.util.Date;

import javax.crypto.SecretKey;

import com.gyan.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long ACCESS_TOKEN_TTL_MILLIS = 15 * 60 * 1000L;
    
    public static String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_TTL_MILLIS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); 
    }
}
