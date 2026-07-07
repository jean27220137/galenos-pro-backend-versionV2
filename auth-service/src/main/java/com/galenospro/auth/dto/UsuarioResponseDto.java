package com.galenospro.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UsuarioResponseDto {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private String cargo;
    private String rol;
    private Long farmaciaId;
    private Integer activo;
    private LocalDate fechaCreacion;
}
