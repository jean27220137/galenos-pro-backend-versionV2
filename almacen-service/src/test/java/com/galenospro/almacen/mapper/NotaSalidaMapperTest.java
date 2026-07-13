package com.galenospro.almacen.mapper;

import com.galenospro.almacen.entity.DetalleNotaSalida;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NotaSalidaMapperTest {

    private final NotaSalidaMapper mapper = Mockito.mock(NotaSalidaMapper.class, Mockito.CALLS_REAL_METHODS);

    @Test
    void calcularTotal_cantidad_nula_retorna_cero() {
        DetalleNotaSalida detalle = new DetalleNotaSalida();
        detalle.setCantidad(null);
        detalle.setPrecioUnitario(BigDecimal.valueOf(10.5));

        BigDecimal total = mapper.calcularTotal(detalle);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calcularTotal_precio_nulo_retorna_cero() {
        DetalleNotaSalida detalle = new DetalleNotaSalida();
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(null);

        BigDecimal total = mapper.calcularTotal(detalle);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calcularTotal_ambos_presentes_retorna_producto() {
        DetalleNotaSalida detalle = new DetalleNotaSalida();
        detalle.setCantidad(10);
        detalle.setPrecioUnitario(BigDecimal.valueOf(5.25));

        BigDecimal total = mapper.calcularTotal(detalle);

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(52.50));
    }
}
