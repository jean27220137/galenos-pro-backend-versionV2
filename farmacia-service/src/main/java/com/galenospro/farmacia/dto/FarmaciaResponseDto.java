package com.galenospro.farmacia.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FarmaciaResponseDto {
    private Long   id;
    private String codigo;
    private String nombre;
    private String area;
    private String tipo;
}
