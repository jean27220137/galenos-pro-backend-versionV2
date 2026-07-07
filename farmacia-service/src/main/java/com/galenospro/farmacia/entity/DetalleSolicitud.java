package com.galenospro.farmacia.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DETALLE_SOLICITUD", schema = "GP_FARMACIA")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SOLICITUD_ID", nullable = false)
    private Long solicitudId;

    @Column(name = "MEDICAMENTO_ID", nullable = false)
    private Long medicamentoId;

    @Column(name = "CANTIDAD_SOLICITADA", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "CANTIDAD_APROBADA")
    private Integer cantidadAprobada;

    @Column(name = "OBSERVACION", length = 300)
    private String observacion;
}
