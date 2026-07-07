package com.galenospro.farmacia.service;

import com.galenospro.farmacia.dto.SolicitudRequestDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;

import java.util.List;
import java.util.Map;

public interface SolicitudService {
    SolicitudResponseDto crear(SolicitudRequestDto dto, Long farmaceuticoId);
    List<SolicitudResponseDto> listarPorFarmacia(Long farmaciaId);
    List<SolicitudResponseDto> listarPorEstado(String estado);
    SolicitudResponseDto buscarPorId(Long id);
    String consultarEstado(Long id);
    void cancelar(Long id);
    void marcarEnProceso(Long id);
    void procesarDespachoConfirmado(Map<String, Object> payload);
}
