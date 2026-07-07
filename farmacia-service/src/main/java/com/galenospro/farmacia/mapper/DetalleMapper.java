package com.galenospro.farmacia.mapper;

import com.galenospro.farmacia.dto.DetalleResponseDto;
import com.galenospro.farmacia.entity.DetalleSolicitud;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DetalleMapper {
    DetalleResponseDto toDto(DetalleSolicitud detalle);
    List<DetalleResponseDto> toDtoList(List<DetalleSolicitud> detalles);
}
