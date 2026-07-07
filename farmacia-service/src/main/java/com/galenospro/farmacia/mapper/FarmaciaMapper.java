package com.galenospro.farmacia.mapper;

import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.entity.Farmacia;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FarmaciaMapper {
    FarmaciaResponseDto toDto(Farmacia farmacia);
    List<FarmaciaResponseDto> toDtoList(List<Farmacia> farmacias);
}
