package com.galenospro.farmacia.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleSolicitudNotFound_retorna_404() {
        ResponseEntity<Map<String, Object>> resp = handler.handleSolicitudNotFound(new SolicitudNotFoundException(1L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleFarmaciaNotFound_retorna_404() {
        ResponseEntity<Map<String, Object>> resp = handler.handleFarmaciaNotFound(new FarmaciaNotFoundException(1L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleDuplicada_retorna_409() {
        ResponseEntity<Map<String, Object>> resp = handler.handleDuplicada(new SolicitudDuplicadaException(1L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleEstadoInvalido_retorna_409() {
        ResponseEntity<Map<String, Object>> resp = handler.handleEstadoInvalido(new EstadoInvalidoException("estado inválido"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleIllegalArgument_retorna_409() {
        ResponseEntity<Map<String, Object>> resp = handler.handleIllegalArgument(new IllegalArgumentException("arg inválido"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleAccessDenied_retorna_403() {
        ResponseEntity<Map<String, Object>> resp = handler.handleAccessDenied(new AccessDeniedException("denegado"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody()).containsEntry("error", "Acceso denegado");
    }

    @Test
    void handleValidacion_retorna_400_con_mensajes() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "solicitud");
        binding.addError(new FieldError("solicitud", "farmaciaId", "no debe ser nulo"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, binding);

        ResponseEntity<Map<String, Object>> resp = handler.handleValidacion(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("error").toString()).contains("farmaciaId");
    }

    @Test
    void handleGeneral_retorna_500() {
        ResponseEntity<Map<String, Object>> resp = handler.handleGeneral(new RuntimeException("error inesperado"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
