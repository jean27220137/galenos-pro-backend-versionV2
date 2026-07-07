package com.galenospro.almacen.mapper;

import com.galenospro.almacen.dto.StockResponseDto;
import com.galenospro.almacen.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "codigoSismed", ignore = true)
    @Mapping(target = "nombreMedicamento", ignore = true)
    StockResponseDto toDto(Stock stock);

    List<StockResponseDto> toDtoList(List<Stock> stocks);
}
