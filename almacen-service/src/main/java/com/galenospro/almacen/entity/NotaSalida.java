package com.galenospro.almacen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "NOTA_SALIDA", schema = "GP_ALMACEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaSalida {

    @Id
    @SequenceGenerator(name = "SEQ_NOTA_SALIDA", sequenceName = "GP_ALMACEN.SEQ_NOTA_SALIDA", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NOTA_SALIDA")
    private Long id;

    @Column(name = "NRO_NOTA_SALIDA", nullable = false, unique = true, length = 20)
    private String nroNotaSalida;

    @Column(name = "SOLICITUD_ID", nullable = false)
    private Long solicitudId;

    @Column(name = "ALMACEN_ORIGEN_ID", nullable = false)
    private Long almacenOrigenId;

    @Column(name = "ALMACEN_DESTINO_ID", nullable = false)
    private Long almacenDestinoId;

    @Column(name = "NRO_MOVIMIENTO", nullable = false, length = 20)
    private String nroMovimiento;

    @Column(name = "FECHA_MOVIMIENTO", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "DESPACHADO_POR", nullable = false)
    private Long despachadoPor;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTA_ID")
    private List<DetalleNotaSalida> detalles;

    @PrePersist
    void prePersist() {
        if (estado == null) estado = "GENERADA";
        if (fechaMovimiento == null) fechaMovimiento = LocalDateTime.now();
    }
}
