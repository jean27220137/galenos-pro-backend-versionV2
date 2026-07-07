package com.galenospro.farmacia.messaging;

import com.galenospro.farmacia.service.SolicitudService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DespachoConsumerTest {

    @Mock SolicitudService solicitudService;
    @InjectMocks DespachoConsumer despachoConsumer;

    @Test
    void procesarDespacho_exitoso_llama_servicio() {
        Map<String, Object> payload = Map.of(
                "solicitudId", 1L,
                "notaId",      5L,
                "nroNota",     "NS-000001"
        );

        despachoConsumer.procesarDespacho(payload);

        verify(solicitudService).procesarDespachoConfirmado(payload);
    }

    @Test
    void procesarDespacho_con_excepcion_no_propaga_error() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("solicitudId", 99L);

        doThrow(new RuntimeException("Error simulado"))
                .when(solicitudService).procesarDespachoConfirmado(payload);

        despachoConsumer.procesarDespacho(payload);

        verify(solicitudService).procesarDespachoConfirmado(payload);
    }
}
