package com.galenospro.almacen.messaging;

import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.service.NotaSalidaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class SolicitudConsumerTest {

    @Mock NotaSalidaService notaSalidaService;
    @Mock AlmacenPublisher almacenPublisher;

    @InjectMocks SolicitudConsumer consumer;

    @Test
    void procesarSolicitud_log_sin_excepcion() {
        DespachoSolicitudDto dto = new DespachoSolicitudDto();
        dto.setSolicitudId(1L);
        dto.setAlmacenId(1L);
        dto.setFarmaciaId(2L);
        dto.setDetalles(List.of());

        assertThatCode(() -> consumer.procesarSolicitud(dto))
                .doesNotThrowAnyException();
    }

    @Test
    void procesarSolicitud_con_id_nulo_no_lanza_excepcion() {
        DespachoSolicitudDto dto = new DespachoSolicitudDto();

        assertThatCode(() -> consumer.procesarSolicitud(dto))
                .doesNotThrowAnyException();
    }
}
