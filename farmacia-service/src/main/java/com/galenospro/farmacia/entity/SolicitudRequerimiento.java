package com.galenospro.farmacia.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "SOLICITUD_REQUERIMIENTO", schema = "GP_FARMACIA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequerimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NRO_SOLICITUD", nullable = false, unique = true, length = 20)
    private String nroSolicitud;

    @Column(name = "FARMACIA_ID", nullable = false)
    private Long farmaciaId;

    @Column(name = "ALMACEN_ID", nullable = false)
    private Long almacenId;

    @Column(name = "FARMACEUTICO_ID", nullable = false)
    private Long farmaceuticoId;

    @Column(name = "FECHA_SOLICITUD", nullable = false)
    private LocalDate fechaSolicitud;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "OBSERVACION", length = 500)
    private String observacion;

    @Column(name = "NOTA_SALIDA_ID")
    private Long notaSalidaId;

    @Column(name = "FECHA_DESPACHO")
    private LocalDate fechaDespacho;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOLICITUD_ID")
    private List<DetalleSolicitud> detalles;

    @PrePersist
    void prePersist() {
        if (estado == null) estado = "PENDIENTE";
        if (fechaSolicitud == null) fechaSolicitud = LocalDate.now();
    }
}
