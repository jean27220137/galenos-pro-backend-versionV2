package com.galenospro.almacen.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "DETALLE_NOTA_SALIDA", schema = "GP_ALMACEN")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleNotaSalida {

    @Id
    @SequenceGenerator(name = "SEQ_DET_NOTA", sequenceName = "GP_ALMACEN.SEQ_DET_NOTA", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DET_NOTA")
    private Long id;

    @Column(name = "NOTA_ID", nullable = false)
    private Long notaId;

    @Column(name = "MEDICAMENTO_ID", nullable = false)
    private Long medicamentoId;

    @Column(name = "LOTE", nullable = false, length = 50)
    private String lote;

    @Column(name = "FECHA_VENCIMIENTO", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "PRECIO_UNITARIO", nullable = false, precision = 10, scale = 4)
    private BigDecimal precioUnitario;
}
