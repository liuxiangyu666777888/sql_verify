package com.sqljudge.exam.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMinutes * 60_000L);
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .claim("uid", principal.getUserId())
                .claim("role", principal.getRole())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
