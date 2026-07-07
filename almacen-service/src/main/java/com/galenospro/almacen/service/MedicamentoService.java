package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.MedicamentoRequestDto;
import com.galenospro.almacen.dto.MedicamentoResponseDto;
import java.util.List;

public interface MedicamentoService {
    MedicamentoResponseDto crear(MedicamentoRequestDto dto);
    List<MedicamentoResponseDto> listar();
    MedicamentoResponseDto buscarPorId(Long id);
}
