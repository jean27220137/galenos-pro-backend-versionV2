package com.galenospro.farmacia.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetalleRequestDto {
    @NotNull private Long    medicamentoId;
    @Min(1)  private int     cantidadSolicitada;
             private String  observacion;
}
