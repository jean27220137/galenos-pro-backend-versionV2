package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.ProximoVencerDTO;
import com.galenospro.almacen.dto.StockCriticoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DataSource dataSource;

    @Override
    public List<StockCriticoDTO> getStockCritico() {
        log.debug("Consultando medicamentos con stock crítico");
        return ejecutarFnDashboardCritico();
    }

    @Override
    public List<ProximoVencerDTO> getProximosAVencer(int dias) {
        log.debug("Consultando lotes próximos a vencer en {} días", dias);
        return ejecutarFnProximosVencer(dias);
    }

    // ── métodos package-private para facilitar pruebas con @Spy ───────────────

    @SuppressWarnings("unchecked")
    List<StockCriticoDTO> ejecutarFnDashboardCritico() {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("GP_ALMACEN")
                .withCatalogName("PKG_STOCK")
                .withFunctionName("FN_DASHBOARD_CRITICO")
                .returningResultSet("return",
                        BeanPropertyRowMapper.newInstance(StockCriticoDTO.class));
        Map<String, Object> result = call.execute();
        List<StockCriticoDTO> lista = (List<StockCriticoDTO>) result.get("return");
        return lista != null ? lista : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    List<ProximoVencerDTO> ejecutarFnProximosVencer(int dias) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withSchemaName("GP_ALMACEN")
                .withCatalogName("PKG_STOCK")
                .withFunctionName("FN_PROXIMOS_VENCER")
                .returningResultSet("return",
                        BeanPropertyRowMapper.newInstance(ProximoVencerDTO.class));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_dias", dias);
        Map<String, Object> result = call.execute(params);
        List<ProximoVencerDTO> lista = (List<ProximoVencerDTO>) result.get("return");
        return lista != null ? lista : Collections.emptyList();
    }
}
