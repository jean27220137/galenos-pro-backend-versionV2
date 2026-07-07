package com.galenospro.almacen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCriticoDTO {

    private String  medicamentoNombre;
    private String  codigoSismed;
    private String  presentacion;
    private Integer cantidadActual;
    private Integer stockMinimo;
}
