package com.galenospro.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.gateway.config.JwtValidatorConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtValidatorConfig jwtValidator;
    @Mock private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtValidator, new ObjectMapper());
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void deberia_retornar_401_sin_token() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void deberia_retornar_401_token_invalido() {
        when(jwtValidator.validarToken("token-malo"))
                .thenThrow(new JwtException("firma inválida"));

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-malo")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void deberia_inyectar_headers_con_token_valido_sin_farmacia() {
        Claims claims = mock(Claims.class);
        when(claims.get("farmaciaId", Long.class)).thenReturn(null);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.get("rol", String.class)).thenReturn("FARMACEUTICO");
        when(jwtValidator.validarToken("token-ok")).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-ok")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isNotEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain).filter(any());
    }

    @Test
    void deberia_inyectar_headers_con_token_valido_con_farmacia() {
        Claims claims = mock(Claims.class);
        when(claims.get("farmaciaId", Long.class)).thenReturn(3L);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.get("rol", String.class)).thenReturn("ALMACENERO");
        when(jwtValidator.validarToken("token-ok-almacen")).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/almacen/stock")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-ok-almacen")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isNotEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain).filter(any());
    }

    @Test
    void deberia_retornar_401_header_sin_prefijo_bearer() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/almacen/stock")
                .header(HttpHeaders.AUTHORIZATION, "token-sin-bearer")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void deberia_usar_fallback_bytes_cuando_objectMapper_falla() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("fail") {});

        JwtAuthenticationFilter failFilter = new JwtAuthenticationFilter(jwtValidator, failingMapper);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/farmacia/solicitudes").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(failFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }
}
