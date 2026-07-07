package com.galenospro.farmacia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetalleResponseDto {
    private Long    id;
    private Long    medicamentoId;
    private Integer cantidadSolicitada;
    private Integer cantidadAprobada;
    private String  observacion;
}
