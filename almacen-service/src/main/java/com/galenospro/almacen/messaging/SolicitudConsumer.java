package com.galenospro.almacen.messaging;

import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.service.NotaSalidaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudConsumer {

    private final NotaSalidaService notaSalidaService;
    private final AlmacenPublisher almacenPublisher;

    @RabbitListener(queues = "${galenos.rabbitmq.cola.solicitud}")
    public void procesarSolicitud(DespachoSolicitudDto dto) {
        log.info("Solicitud nueva recibida id={} — pendiente de atención manual por almacenero",
                dto.getSolicitudId());
    }
}
