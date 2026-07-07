package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import java.util.List;

public interface NotaSalidaService {
    NotaSalidaResponseDto buscarPorId(Long id);
    List<NotaSalidaResponseDto> listarPorAlmacen(Long almacenId);
    void confirmarEntrega(Long notaId);
    NotaSalidaResponseDto despacharSolicitud(DespachoSolicitudDto dto);
}
