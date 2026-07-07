package com.galenospro.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "USUARIO", schema = "GP_AUTH")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USUARIO")
    @SequenceGenerator(name = "SEQ_USUARIO",
                       sequenceName = "GP_AUTH.SEQ_USUARIO",
                       allocationSize = 1)
    private Long id;

    @Column(name = "NOMBRES", nullable = false, length = 100)
    private String nombres;

    @Column(name = "APELLIDOS", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "EMAIL", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "CARGO", length = 100)
    private String cargo;

    @Column(name = "ROL", nullable = false, length = 30)
    private String rol;

    @Column(name = "FARMACIA_ID")
    private Long farmaciaId;

    @Column(name = "ACTIVO", nullable = false)
    private Integer activo;

    @Column(name = "FECHA_CREACION")
    private LocalDate fechaCreacion;

    @PrePersist
    void prePersist() {
        if (activo == null) activo = 1;
        if (fechaCreacion == null) fechaCreacion = LocalDate.now();
    }
}
