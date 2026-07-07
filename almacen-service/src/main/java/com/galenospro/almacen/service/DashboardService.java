package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.ProximoVencerDTO;
import com.galenospro.almacen.dto.StockCriticoDTO;

import java.util.List;

public interface DashboardService {

    List<StockCriticoDTO> getStockCritico();

    List<ProximoVencerDTO> getProximosAVencer(int dias);
}
