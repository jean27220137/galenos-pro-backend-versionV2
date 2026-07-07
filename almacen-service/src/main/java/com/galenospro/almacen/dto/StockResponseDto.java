package com.galenospro.almacen.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class StockResponseDto {
    private Long       id;
    private Long       medicamentoId;
    private String     codigoSismed;
    private String     nombreMedicamento;
    private Long       almacenId;
    private String     lote;
    private Integer    cantidad;
    private LocalDate  fechaVencimiento;
    private BigDecimal precioUnitario;
}
