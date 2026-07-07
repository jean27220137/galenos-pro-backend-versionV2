package com.galenospro.almacen.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DetalleNotaSalidaResponseDto {
    private Long       id;
    private Long       medicamentoId;
    private String     lote;
    private LocalDate  fechaVencimiento;
    private Integer    cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total;
}
