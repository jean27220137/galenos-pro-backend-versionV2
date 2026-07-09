package com.galenospro.almacen.exception;

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
    void handleMedicamentoNotFound_retorna_404() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleMedicamentoNotFound(new MedicamentoNotFoundException(10L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void handleNotaSalidaNotFound_retorna_404() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleNotaSalidaNotFound(new NotaSalidaNotFoundException(5L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void handleAlmacenNotFound_retorna_404() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleAlmacenNotFound(new AlmacenNotFoundException(3L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsKey("error");
    }

    @Test
    void handleStockInsuficiente_retorna_409() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleStockInsuficiente(new StockInsuficienteException(7L));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().get("error").toString()).contains("Stock insuficiente");
    }

    @Test
    void handleValidacion_retorna_400_con_errores() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(
                List.of(new FieldError("dto", "cantidad", "debe ser mayor que 0")));

        ResponseEntity<Map<String, Object>> resp = handler.handleValidacion(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("error").toString()).contains("cantidad");
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
