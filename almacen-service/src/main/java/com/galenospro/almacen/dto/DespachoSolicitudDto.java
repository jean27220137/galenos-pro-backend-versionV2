package com.galenospro.almacen.dto;

import lombok.Data;
import java.util.List;

@Data
public class DespachoSolicitudDto {
    private Long solicitudId;
    private Long almacenId;
    private Long almacenDestinoId;
    private Long farmaciaId;
    private Long despachadorId;
    private List<DetalleDespachoDto> detalles;

    @Data
    public static class DetalleDespachoDto {
        private Long    medicamentoId;
        private Integer cantidadSolicitada;
    }
}
