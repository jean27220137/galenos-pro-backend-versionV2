package com.galenospro.farmacia.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SolicitudResponseDto {
    private Long                  id;
    private String                nroSolicitud;
    private Long                  farmaciaId;
    private Long                  almacenId;
    private Long                  farmaceuticoId;
    private LocalDate             fechaSolicitud;
    private String                estado;
    private String                observacion;
    private Long                  notaSalidaId;
    private List<DetalleResponseDto> detalles;
}
