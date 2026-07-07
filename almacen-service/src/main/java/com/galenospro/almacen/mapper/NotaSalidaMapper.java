package com.galenospro.almacen.mapper;

import com.galenospro.almacen.dto.DetalleNotaSalidaResponseDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.entity.DetalleNotaSalida;
import com.galenospro.almacen.entity.NotaSalida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NotaSalidaMapper {

    NotaSalidaResponseDto toDto(NotaSalida nota);

    List<NotaSalidaResponseDto> toDtoList(List<NotaSalida> notas);

    @Mapping(target = "total", expression = "java(calcularTotal(detalle))")
    DetalleNotaSalidaResponseDto detalleToDto(DetalleNotaSalida detalle);

    default BigDecimal calcularTotal(DetalleNotaSalida d) {
        if (d.getCantidad() == null || d.getPrecioUnitario() == null) return BigDecimal.ZERO;
        return d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
    }
}
