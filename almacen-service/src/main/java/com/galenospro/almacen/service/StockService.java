package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.EntradaStockRequestDto;
import com.galenospro.almacen.dto.StockResponseDto;
import java.util.List;

public interface StockService {
    StockResponseDto registrarEntrada(EntradaStockRequestDto dto);
    Integer consultarDisponible(Long medicamentoId, Long almacenId);
    List<StockResponseDto> listarPorAlmacen(Long almacenId);
    List<StockResponseDto> listarPorMedicamento(Long medicamentoId, Long almacenId);
}
