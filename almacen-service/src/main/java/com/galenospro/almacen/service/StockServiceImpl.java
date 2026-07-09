package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.EntradaStockRequestDto;
import com.galenospro.almacen.dto.StockResponseDto;
import com.galenospro.almacen.entity.Stock;
import com.galenospro.almacen.mapper.StockMapper;
import com.galenospro.almacen.repository.MedicamentoRepository;
import com.galenospro.almacen.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final StockMapper stockMapper;
    private final DataSource dataSource;

    @Override
    @CacheEvict(value = "stock", key = "#dto.almacenId + ':' + #dto.medicamentoId")
    public StockResponseDto registrarEntrada(EntradaStockRequestDto dto) {
        Long id = llamarPrRegistrarEntrada(dataSource, dto);
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error al recuperar stock id=" + id));
        log.info("Entrada de stock registrada: medicamento={} lote={} cantidad={}",
                dto.getMedicamentoId(), dto.getLote(), dto.getCantidad());
        return enriquecerDto(stockMapper.toDto(stock));
    }

    @Override
    @Cacheable(value = "stock", key = "#almacenId + ':' + #medicamentoId")
    public Integer consultarDisponible(Long medicamentoId, Long almacenId) {
        Integer total = stockRepository.sumCantidadDisponible(medicamentoId, almacenId);
        return total != null ? total : 0;
    }

    @Override
    public List<StockResponseDto> listarPorAlmacen(Long almacenId) {
        return stockRepository.findByAlmacenId(almacenId).stream()
                .map(s -> enriquecerDto(stockMapper.toDto(s)))
                .collect(Collectors.toList());
    }

    @Override
    public List<StockResponseDto> listarPorMedicamento(Long medicamentoId, Long almacenId) {
        return stockRepository
                .findByMedicamentoIdAndAlmacenIdOrderByFechaVencimientoAsc(medicamentoId, almacenId)
                .stream()
                .map(s -> enriquecerDto(stockMapper.toDto(s)))
                .collect(Collectors.toList());
    }

    Long llamarPrRegistrarEntrada(DataSource ds, EntradaStockRequestDto dto) {
        SimpleJdbcCall call = new SimpleJdbcCall(ds)
                .withSchemaName("GP_ALMACEN")
                .withCatalogName("PKG_STOCK")
                .withProcedureName("PR_REGISTRAR_ENTRADA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlParameter("p_medicamento_id",  Types.NUMERIC),
                    new SqlParameter("p_almacen_id",      Types.NUMERIC),
                    new SqlParameter("p_lote",            Types.VARCHAR),
                    new SqlParameter("p_cantidad",        Types.NUMERIC),
                    new SqlParameter("p_fecha_vto",       Types.DATE),
                    new SqlParameter("p_precio_unitario", Types.NUMERIC),
                    new SqlOutParameter("p_id",           Types.NUMERIC)
                );
        Map<String, Object> result = call.execute(Map.of(
                "p_medicamento_id",  dto.getMedicamentoId(),
                "p_almacen_id",      dto.getAlmacenId(),
                "p_lote",            dto.getLote(),
                "p_cantidad",        dto.getCantidad(),
                "p_fecha_vto",       Date.valueOf(dto.getFechaVencimiento()),
                "p_precio_unitario", dto.getPrecioUnitario()
        ));
        return ((Number) result.get("p_id")).longValue();
    }

    private StockResponseDto enriquecerDto(StockResponseDto dto) {
        medicamentoRepository.findById(dto.getMedicamentoId()).ifPresent(m -> {
            dto.setCodigoSismed(m.getCodigoSismed());
            dto.setNombreMedicamento(m.getNombre());
        });
        return dto;
    }
}
