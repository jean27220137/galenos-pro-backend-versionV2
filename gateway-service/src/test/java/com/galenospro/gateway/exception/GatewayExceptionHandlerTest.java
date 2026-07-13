package com.galenospro.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GatewayExceptionHandlerTest {

    @Test
    void handle_excepcion_generica_retorna_500() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler(new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").build());

        StepVerifier.create(handler.handle(exchange, new RuntimeException("error genérico")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handle_ResponseStatusException_con_reason_retorna_status_y_reason() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler(new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").build());

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada");

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handle_ResponseStatusException_sin_reason_usa_mensaje_por_defecto() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler(new ObjectMapper());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").build());

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);

        StepVerifier.create(handler.handle(exchange, ex))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handle_objectMapper_falla_usa_fallback_bytes() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsBytes(any())).thenThrow(new JsonProcessingException("error serialización") {});

        GatewayExceptionHandler handler = new GatewayExceptionHandler(failingMapper);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").build());

        StepVerifier.create(handler.handle(exchange, new RuntimeException("error")))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
