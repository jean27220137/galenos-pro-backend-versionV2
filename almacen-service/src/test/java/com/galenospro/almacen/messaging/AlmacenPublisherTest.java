package com.galenospro.almacen.messaging;

import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlmacenPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;

    @InjectMocks AlmacenPublisher almacenPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(almacenPublisher, "exchange",         "galenos.exchange");
        ReflectionTestUtils.setField(almacenPublisher, "colaDespacho",     "cola.despacho.confirmado");
        ReflectionTestUtils.setField(almacenPublisher, "colaStockCritico", "cola.stock.critico");
    }

    @Test
    void publicarDespachoConfirmado_con_nota_envia_payload_completo() {
        NotaSalidaResponseDto nota = NotaSalidaResponseDto.builder()
                .id(1L).nroNotaSalida("NS-000001").nroMovimiento("MOV-001")
                .estado("GENERADA").build();

        DespachoSolicitudDto dto = new DespachoSolicitudDto();
        dto.setSolicitudId(1L);
        dto.setFarmaciaId(3L);

        almacenPublisher.publicarDespachoConfirmado(nota, dto);

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq("galenos.exchange"), eq("despacho.confirmado"), captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertThat(payload).containsKey("notaId");
        assertThat(payload.get("notaId")).isEqualTo(1L);
        assertThat(payload.get("nroNota")).isEqualTo("NS-000001");
        assertThat(payload.get("solicitudId")).isEqualTo(1L);
        assertThat(payload.get("farmaciaId")).isEqualTo(3L);
    }

    @Test
    void publicarDespachoConfirmado_con_nota_null_envia_payload_con_error() {
        DespachoSolicitudDto dto = new DespachoSolicitudDto();
        dto.setSolicitudId(2L);
        dto.setFarmaciaId(4L);

        almacenPublisher.publicarDespachoConfirmado(null, dto);

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq("galenos.exchange"), eq("despacho.confirmado"), captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertThat(payload).containsKey("error");
        assertThat(payload.get("error").toString()).contains("Error al generar");
    }

    @Test
    void publicarStockCritico_envia_lista_de_criticos() {
        List<Map<String, Object>> criticos = List.of(
                Map.of("medicamentoId", 10L, "nombre", "Paracetamol 500mg", "cantidad", 2));

        almacenPublisher.publicarStockCritico(1L, criticos);

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(rabbitTemplate).convertAndSend(
                eq("galenos.exchange"), eq("stock.critico"), captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertThat(payload.get("almacenId")).isEqualTo(1L);
        assertThat(payload.get("medicamentosCriticos")).isEqualTo(criticos);
    }
}
