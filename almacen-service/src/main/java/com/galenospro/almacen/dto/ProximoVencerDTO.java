package com.galenospro.almacen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProximoVencerDTO {

    private String    medicamentoNombre;
    private String    codigoSismed;
    private String    lote;
    private LocalDate fechaVencimiento;
    private Integer   diasRestantes;
    private Integer   cantidad;
    private String    almacenNombre;
}
