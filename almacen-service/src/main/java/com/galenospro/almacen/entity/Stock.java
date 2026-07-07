package com.galenospro.almacen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "STOCK", schema = "GP_ALMACEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @SequenceGenerator(name = "SEQ_STOCK", sequenceName = "GP_ALMACEN.SEQ_STOCK", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STOCK")
    private Long id;

    @Column(name = "MEDICAMENTO_ID", nullable = false)
    private Long medicamentoId;

    @Column(name = "ALMACEN_ID", nullable = false)
    private Long almacenId;

    @Column(name = "LOTE", nullable = false, length = 50)
    private String lote;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "FECHA_VENCIMIENTO", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "PRECIO_UNITARIO", nullable = false, precision = 10, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "FECHA_INGRESO", nullable = false, updatable = false)
    private LocalDate fechaIngreso;

    @PrePersist
    void prePersist() {
        if (fechaIngreso == null) fechaIngreso = LocalDate.now();
        if (cantidad == null) cantidad = 0;
    }
}
