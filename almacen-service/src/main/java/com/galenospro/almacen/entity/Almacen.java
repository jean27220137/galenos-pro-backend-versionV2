package com.galenospro.almacen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "ALMACEN", schema = "GP_ALMACEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Almacen {

    @Id
    @SequenceGenerator(name = "SEQ_ALMACEN", sequenceName = "GP_ALMACEN.SEQ_ALMACEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ALMACEN")
    private Long id;

    @Column(name = "CODIGO", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "DIRECCION", length = 300)
    private String direccion;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDate fechaCreacion;

    @PrePersist
    void prePersist() {
        if (activo == null) activo = 1;
        if (fechaCreacion == null) fechaCreacion = LocalDate.now();
    }
}
