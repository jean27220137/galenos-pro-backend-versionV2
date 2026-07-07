package com.galenospro.almacen.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NotaSalidaResponseDto {
    private Long                          id;
    private String                        nroNotaSalida;
    private Long                          solicitudId;
    private Long                          almacenOrigenId;
    private Long                          almacenDestinoId;
    private String                        nroMovimiento;
    private LocalDateTime                 fechaMovimiento;
    private String                        estado;
    private Long                          despachadoPor;
    private List<DetalleNotaSalidaResponseDto> detalles;
}
