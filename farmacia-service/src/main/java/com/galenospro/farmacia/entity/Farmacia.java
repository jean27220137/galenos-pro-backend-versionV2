package com.galenospro.farmacia.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FARMACIA", schema = "GP_FARMACIA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farmacia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CODIGO", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 200)
    private String nombre;

    @Column(name = "AREA", length = 100)
    private String area;

    @Column(name = "TIPO", length = 50)
    private String tipo;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo;

    @PrePersist
    void prePersist() {
        if (activo == null) activo = 1;
    }
}
