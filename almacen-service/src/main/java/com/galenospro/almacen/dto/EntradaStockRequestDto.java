package com.galenospro.almacen.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EntradaStockRequestDto {
    @NotNull  private Long      medicamentoId;
    @NotNull  private Long      almacenId;
    @NotBlank private String    lote;
    @Min(1)   private Integer   cantidad;
    @NotNull  private LocalDate fechaVencimiento;
    @DecimalMin("0.0001") private BigDecimal precioUnitario;
}
