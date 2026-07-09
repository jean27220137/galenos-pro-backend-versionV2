package com.galenospro.farmacia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FarmaciaResponseDto {
    private Long    id;
    private String  codigo;
    private String  nombre;
    private String  tipo;
    private String  area;
    private String  ubicacion;
    private String  departamento;
    private Long    jefeId;
    private String  jefeNombre;
    private String  telefono;
    private Integer activo;
}
