package com.galenospro.almacen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MEDICAMENTO", schema = "GP_ALMACEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medicamento {

    @Id
    @SequenceGenerator(name = "SEQ_MEDICAMENTO", sequenceName = "GP_ALMACEN.SEQ_MEDICAMENTO", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDICAMENTO")
    private Long id;

    @Column(name = "CODIGO_SISMED", nullable = false, unique = true, length = 20)
    private String codigoSismed;

    @Column(name = "NOMBRE", nullable = false, length = 200)
    private String nombre;

    @Column(name = "PRESENTACION", length = 100)
    private String presentacion;

    @Column(name = "CONCENTRACION", length = 100)
    private String concentracion;

    @Column(name = "VIA_ADMINISTRACION", length = 50)
    private String viaAdministracion;

    @Column(name = "UNIDAD_MEDIDA", length = 30)
    private String unidadMedida;

    @Column(name = "STOCK_MINIMO", nullable = false)
    private Integer stockMinimo;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo;

    @PrePersist
    void prePersist() {
        if (activo == null) activo = 1;
        if (stockMinimo == null) stockMinimo = 10;
    }
}
