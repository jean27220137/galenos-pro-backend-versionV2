package com.galenospro.farmacia.service;

import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import java.util.List;

public interface FarmaciaService {
    List<FarmaciaResponseDto> listar();
    FarmaciaResponseDto buscarPorId(Long id);
}
