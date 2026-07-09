package com.galenospro.farmacia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmaciaRequestDto {

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "FAR-\\d{3}", message = "El código debe tener formato FAR-XXX")
    private String codigo;

    @NotBlank
    @Size(max = 200)
    private String nombre;

    @NotBlank
    private String tipo;

    @Size(max = 100)
    private String area;

    @Size(max = 200)
    private String ubicacion;

    @Size(max = 100)
    private String departamento;

    private Long   jefeId;

    @Size(max = 200)
    private String jefeNombre;

    @Size(max = 15)
    private String telefono;
}
