package com.galenospro.almacen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.exception.NotaSalidaNotFoundException;
import com.galenospro.almacen.mapper.NotaSalidaMapper;
import com.galenospro.almacen.repository.NotaSalidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotaSalidaServiceImpl implements NotaSalidaService {

    private final NotaSalidaRepository notaSalidaRepository;
    private final NotaSalidaMapper notaSalidaMapper;
    private final ObjectMapper objectMapper;
    private final DataSource dataSource;

    @Override
    public NotaSalidaResponseDto buscarPorId(Long id) {
        return notaSalidaMapper.toDto(
                notaSalidaRepository.findById(id)
                        .orElseThrow(() -> new NotaSalidaNotFoundException(id))
        );
    }

    @Override
    public List<NotaSalidaResponseDto> listarPorAlmacen(Long almacenId) {
        return notaSalidaMapper.toDtoList(notaSalidaRepository.findByAlmacenOrigenId(almacenId));
    }

    @Override
    public void confirmarEntrega(Long notaId) {
        llamarPrConfirmarEntrega(dataSource, notaId);
        log.info("Nota de Salida entregada id={}", notaId);
    }

    @Override
    public NotaSalidaResponseDto despacharSolicitud(DespachoSolicitudDto dto) {
        Map<String, Object> resultado = llamarPrDespacharSolicitud(dataSource, dto);
        Long notaId = ((Number) resultado.get("p_nota_id")).longValue();
        log.info("Despacho generado: nota={} solicitud={}",
                resultado.get("p_nro_nota"), dto.getSolicitudId());
        return buscarPorId(notaId);
    }

    Map<String, Object> llamarPrDespacharSolicitud(DataSource ds, DespachoSolicitudDto dto) {
        String detallesJson;
        try {
            detallesJson = objectMapper.writeValueAsString(dto.getDetalles());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando detalles de despacho", e);
        }

        SimpleJdbcCall call = new SimpleJdbcCall(ds)
                .withSchemaName("GP_ALMACEN")
                .withCatalogName("PKG_STOCK")
                .withProcedureName("PR_DESPACHAR_SOLICITUD");

        Map<String, Object> params = new HashMap<>();
        params.put("p_solicitud_id",    dto.getSolicitudId());
        params.put("p_almacen_id",      dto.getAlmacenId());
        params.put("p_almacen_destino", dto.getAlmacenDestinoId());
        params.put("p_despachado_por",  dto.getDespachadorId());
        params.put("p_detalles_json",   detallesJson);

        try {
            return call.execute(params);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20010")) {
                throw new NotaSalidaNotFoundException(dto.getSolicitudId());
            }
            throw ex;
        }
    }

    void llamarPrConfirmarEntrega(DataSource ds, Long notaId) {
        SimpleJdbcCall call = new SimpleJdbcCall(ds)
                .withSchemaName("GP_ALMACEN")
                .withCatalogName("PKG_STOCK")
                .withProcedureName("PR_CONFIRMAR_ENTREGA");
        try {
            call.execute(Map.of("p_nota_id", notaId));
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20011")) {
                throw new NotaSalidaNotFoundException(notaId);
            }
            throw ex;
        }
    }
}
