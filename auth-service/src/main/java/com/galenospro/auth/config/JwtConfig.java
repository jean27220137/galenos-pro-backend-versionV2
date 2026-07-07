package com.galenospro.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-minutes:30}")
    private int expirationMinutes;

    public String generarToken(Long userId, String email, String rol, Long farmaciaId) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + (long) expirationMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("rol", rol)
                .claim("farmaciaId", farmaciaId)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getKey())
                .compact();
    }

    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public LocalDateTime getExpiracion() {
        return LocalDateTime.now().plusMinutes(expirationMinutes);
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
