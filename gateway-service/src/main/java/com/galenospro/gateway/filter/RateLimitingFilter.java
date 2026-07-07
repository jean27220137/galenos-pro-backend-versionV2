package com.galenospro.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements GatewayFilter {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gateway.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    private static final String RATE_PREFIX = "rate:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clave = resolverClave(exchange);

        return reactiveRedisTemplate.opsForValue()
                .increment(RATE_PREFIX + clave)
                .flatMap(contador -> {
                    if (contador == 1) {
                        reactiveRedisTemplate.expire(
                                RATE_PREFIX + clave, Duration.ofMinutes(1)
                        ).subscribe();
                    }

                    if (contador > requestsPerMinute) {
                        log.warn("Rate limit excedido para clave={}", clave);
                        return responderError(exchange,
                                HttpStatus.TOO_MANY_REQUESTS,
                                "Límite de peticiones excedido");
                    }

                    return chain.filter(exchange);
                });
    }

    private String resolverClave(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }
        String ip = Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress()
        ).getAddress().getHostAddress();
        return "ip:" + ip;
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
