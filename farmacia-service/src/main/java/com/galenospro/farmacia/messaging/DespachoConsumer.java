package com.galenospro.farmacia.messaging;

import com.galenospro.farmacia.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DespachoConsumer {

    private final SolicitudService solicitudService;

    @RabbitListener(queues = "${galenos.rabbitmq.cola.despacho}")
    public void procesarDespacho(Map<String, Object> payload) {
        Object solicitudIdObj = payload.get("solicitudId");
        log.info("Despacho confirmado recibido solicitudId={}", solicitudIdObj);
        try {
            solicitudService.procesarDespachoConfirmado(payload);
        } catch (Exception ex) {
            log.error("Error procesando despacho confirmado solicitudId={}: {}",
                    solicitudIdObj, ex.getMessage());
        }
    }
}
