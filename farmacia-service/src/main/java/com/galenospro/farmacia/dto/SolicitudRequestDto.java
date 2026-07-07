package com.galenospro.farmacia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SolicitudRequestDto {
    @NotNull private Long farmaciaId;
    @NotNull private Long almacenId;
    @NotEmpty @Valid private List<DetalleRequestDto> detalles;
}
