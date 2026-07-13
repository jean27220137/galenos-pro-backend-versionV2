package com.galenospro.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock private ReactiveRedisTemplate<String, String> redisTemplate;
    @Mock private ReactiveValueOperations<String, String> valueOps;
    @Mock private GatewayFilterChain chain;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(redisTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(filter, "requestsPerMinute", 100);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void deberia_permitir_peticion_bajo_limite() {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes")
                .remoteAddress(new java.net.InetSocketAddress("127.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode())
                .isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void deberia_retornar_429_sobre_limite() {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(101L));

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes")
                .remoteAddress(new java.net.InetSocketAddress("127.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        verify(chain, never()).filter(any());
    }

    @Test
    void deberia_usar_userId_como_clave_cuando_header_presente() {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes")
                .header("X-User-Id", "42")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void deberia_usar_fallback_bytes_cuando_objectMapper_falla() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("fail") {});

        RateLimitingFilter failFilter = new RateLimitingFilter(redisTemplate, failingMapper);
        ReflectionTestUtils.setField(failFilter, "requestsPerMinute", 100);

        when(valueOps.increment(anyString())).thenReturn(Mono.just(101L));

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes")
                .remoteAddress(new java.net.InetSocketAddress("127.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(failFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
