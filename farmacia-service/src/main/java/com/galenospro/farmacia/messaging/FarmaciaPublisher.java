package com.galenospro.farmacia.messaging;

import com.galenospro.farmacia.dto.SolicitudRequestDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;
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
public class FarmaciaPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${galenos.rabbitmq.exchange}") private String exchange;

    public void publicarSolicitudNueva(SolicitudResponseDto solicitud, SolicitudRequestDto dto) {
        List<Map<String, Object>> detalles = dto.getDetalles().stream()
                .map(d -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("medicamentoId",      d.getMedicamentoId());
                    m.put("cantidadSolicitada", d.getCantidadSolicitada());
                    return m;
                })
                .toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("solicitudId",   solicitud.getId());
        payload.put("nroSolicitud",  solicitud.getNroSolicitud());
        payload.put("farmaciaId",    solicitud.getFarmaciaId());
        payload.put("almacenId",     solicitud.getAlmacenId());
        payload.put("farmaceuticoId", solicitud.getFarmaceuticoId());
        payload.put("detalles",      detalles);

        rabbitTemplate.convertAndSend(exchange, "solicitud.nueva", payload);
        log.info("Publicado solicitud.nueva id={} nro={}", solicitud.getId(), solicitud.getNroSolicitud());
    }
}
