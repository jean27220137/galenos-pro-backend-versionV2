package com.galenospro.almacen.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicamentoRequestDto {
    @NotBlank private String codigoSismed;
    @NotBlank private String nombre;
    private String presentacion;
    private String concentracion;
    private String viaAdministracion;
    private String unidadMedida;
    @Min(0) private Integer stockMinimo;
}
