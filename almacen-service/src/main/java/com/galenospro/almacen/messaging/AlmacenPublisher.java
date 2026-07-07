package com.galenospro.almacen.messaging;

import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlmacenPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${galenos.rabbitmq.exchange}") private String exchange;
    @Value("${galenos.rabbitmq.cola.despacho}") private String colaDespacho;
    @Value("${galenos.rabbitmq.cola.stock-critico}") private String colaStockCritico;

    public void publicarDespachoConfirmado(NotaSalidaResponseDto nota, DespachoSolicitudDto dto) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("solicitudId", dto.getSolicitudId());
        payload.put("farmaciaId",  dto.getFarmaciaId());

        if (nota != null) {
            payload.put("notaId",        nota.getId());
            payload.put("nroNota",        nota.getNroNotaSalida());
            payload.put("nroMovimiento",  nota.getNroMovimiento());
            payload.put("estado",         nota.getEstado());
            payload.put("detalles",       nota.getDetalles());
        } else {
            payload.put("error", "Error al generar Nota de Salida");
        }

        rabbitTemplate.convertAndSend(exchange, "despacho.confirmado", payload);
        log.info("Publicado despacho.confirmado solicitud={}", dto.getSolicitudId());
    }

    public void publicarStockCritico(Long almacenId, List<Map<String, Object>> criticos) {
        Map<String, Object> payload = Map.of(
                "almacenId",             almacenId,
                "medicamentosCriticos",  criticos
        );
        rabbitTemplate.convertAndSend(exchange, "stock.critico", payload);
        log.warn("Stock crítico publicado almacen={} medicamentos={}", almacenId, criticos.size());
    }
}
