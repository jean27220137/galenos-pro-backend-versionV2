package com.galenospro.almacen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoResponseDto {
    private Long   id;
    private String codigoSismed;
    private String nombre;
    private String presentacion;
    private String concentracion;
    private String viaAdministracion;
    private String unidadMedida;
    private Integer stockMinimo;
    private Integer activo;
}
