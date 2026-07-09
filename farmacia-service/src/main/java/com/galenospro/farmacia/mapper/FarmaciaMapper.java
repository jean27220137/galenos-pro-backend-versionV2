package com.galenospro.farmacia.mapper;

import com.galenospro.farmacia.dto.FarmaciaRequestDto;
import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.entity.Farmacia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FarmaciaMapper {

    FarmaciaResponseDto toDto(Farmacia farmacia);

    List<FarmaciaResponseDto> toDtoList(List<Farmacia> farmacias);

    @Mapping(target = "id",     ignore = true)
    @Mapping(target = "activo", ignore = true)
    Farmacia toEntity(FarmaciaRequestDto dto);

    @Mapping(target = "id",     ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateFromDto(FarmaciaRequestDto dto, @MappingTarget Farmacia farmacia);
}
