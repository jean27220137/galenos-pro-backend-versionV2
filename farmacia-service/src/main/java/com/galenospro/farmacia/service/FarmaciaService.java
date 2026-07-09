package com.galenospro.farmacia.service;

import com.galenospro.farmacia.dto.FarmaciaRequestDto;
import com.galenospro.farmacia.dto.FarmaciaResponseDto;

import java.util.List;

public interface FarmaciaService {
    List<FarmaciaResponseDto> listar();
    List<FarmaciaResponseDto> listarTodas();
    FarmaciaResponseDto       buscarPorId(Long id);
    FarmaciaResponseDto       crear(FarmaciaRequestDto dto);
    FarmaciaResponseDto       actualizar(Long id, FarmaciaRequestDto dto);
    void                      desactivar(Long id);
    void                      activar(Long id);
}
