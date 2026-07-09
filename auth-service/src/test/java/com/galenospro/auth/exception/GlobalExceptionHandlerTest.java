package com.galenospro.auth.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleCredencialesInvalidas_retorna_401() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleCredencialesInvalidas(new CredencialesInvalidasException());
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void handleUsuarioInactivo_retorna_401() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleUsuarioInactivo(new UsuarioInactivoException());
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().get("error")).isEqualTo("Usuario inactivo");
    }

    @Test
    void handleUsuarioNotFound_retorna_404() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleUsuarioNotFound(new UsuarioNotFoundException(99L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void handleEmailDuplicado_retorna_409() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleEmailDuplicado(new EmailDuplicadoException("test@bernales.gob.pe"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void handleValidacion_retorna_400_con_errores() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(
                List.of(new FieldError("dto", "email", "El email es obligatorio")));

        ResponseEntity<Map<String, Object>> resp = handler.handleValidacion(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("error").toString()).contains("email");
    }

    @Test
    void handleAccessDenied_retorna_403() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleAccessDenied(new AccessDeniedException("denegado"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody().get("error")).isEqualTo("Acceso denegado");
    }

    @Test
    void handleGeneral_retorna_500() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleGeneral(new RuntimeException("error inesperado"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().get("error")).isEqualTo("Error interno del servidor");
    }
}
