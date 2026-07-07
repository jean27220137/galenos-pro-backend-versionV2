package com.galenospro.almacen.mapper;

import com.galenospro.almacen.dto.MedicamentoRequestDto;
import com.galenospro.almacen.dto.MedicamentoResponseDto;
import com.galenospro.almacen.entity.Medicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MedicamentoMapper {
    MedicamentoResponseDto toDto(Medicamento medicamento);
    List<MedicamentoResponseDto> toDtoList(List<Medicamento> medicamentos);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Medicamento toEntity(MedicamentoRequestDto dto);
}
