package com.galenospro.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.gateway.config.JwtValidatorConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtValidatorConfig jwtValidator;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return responderError(exchange, HttpStatus.UNAUTHORIZED, "Token requerido");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtValidator.validarToken(token);

            ServerHttpRequest requestConHeaders = exchange.getRequest().mutate()
                    .header("X-User-Id",  claims.get("userId", Long.class).toString())
                    .header("X-User-Rol", claims.get("rol", String.class))
                    .build();

            log.info("JWT válido — userId={} rol={}",
                    claims.get("userId"), claims.get("rol"));

            return chain.filter(exchange.mutate().request(requestConHeaders).build());

        } catch (JwtException ex) {
            log.warn("Token inválido o expirado: {}", ex.getMessage());
            return responderError(exchange, HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }
    }

    private Mono<Void> responderError(ServerWebExchange exchange, HttpStatus status, String mensaje) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(Map.of(
                    "status", status.value(),
                    "error", mensaje,
                    "timestamp", LocalDateTime.now().toString()
            ));
        } catch (JsonProcessingException e) {
            body = ("{\"error\":\"" + mensaje + "\"}").getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
