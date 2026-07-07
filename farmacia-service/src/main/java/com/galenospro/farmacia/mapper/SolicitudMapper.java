package com.galenospro.farmacia.mapper;

import com.galenospro.farmacia.dto.DetalleResponseDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;
import com.galenospro.farmacia.entity.SolicitudRequerimiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DetalleMapper.class})
public interface SolicitudMapper {

    @Mapping(target = "detalles", source = "detalles")
    SolicitudResponseDto toDto(SolicitudRequerimiento solicitud);

    List<SolicitudResponseDto> toDtoList(List<SolicitudRequerimiento> solicitudes);
}
