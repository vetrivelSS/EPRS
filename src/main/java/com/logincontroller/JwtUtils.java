package com.logincontroller;

import java.util.Date;
import java.security.Key;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    // Nalla periya secret key (64+ characters)
    private String jwtSecret = "ThisIsAMuchLongerSecretKeyToSatisfyTheHS512AlgorithmRequirement2026_Secure_Strong_Long_Key!!";
    private int jwtExpirationMs = 86400000; // 1 day

    // String-ah safe-ana Key object-ah mathurathuku intha method
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Updated line
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()           // parser() ku bathula parserBuilder()
                .setSigningKey(getSigningKey()) // Updated line
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}