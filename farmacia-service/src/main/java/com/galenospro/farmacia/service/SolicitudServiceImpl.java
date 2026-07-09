package com.galenospro.farmacia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.farmacia.dto.SolicitudRequestDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;
import com.galenospro.farmacia.exception.EstadoInvalidoException;
import com.galenospro.farmacia.exception.SolicitudDuplicadaException;
import com.galenospro.farmacia.exception.SolicitudNotFoundException;
import com.galenospro.farmacia.mapper.SolicitudMapper;
import com.galenospro.farmacia.messaging.FarmaciaPublisher;
import com.galenospro.farmacia.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final SolicitudMapper solicitudMapper;
    private final ObjectMapper objectMapper;
    private final DataSource dataSource;
    private final FarmaciaPublisher farmaciaPublisher;

    @Override
    public SolicitudResponseDto crear(SolicitudRequestDto dto, Long farmaceuticoId) {
        if (dto.getFarmaciaId() == null) {
            throw new EstadoInvalidoException("El usuario no tiene farmacia asignada. Contacte al administrador.");
        }
        Map<String, Object> resultado = llamarPrCrearSolicitud(dataSource, dto, farmaceuticoId);
        Long solicitudId = ((Number) resultado.get("p_solicitud_id")).longValue();
        log.info("Solicitud creada: nro={} farmacia={}", resultado.get("p_nro_solicitud"), dto.getFarmaciaId());

        SolicitudResponseDto response = buscarPorId(solicitudId);
        farmaciaPublisher.publicarSolicitudNueva(response, dto);
        return response;
    }

    @Override
    public List<SolicitudResponseDto> listarPorFarmacia(Long farmaciaId) {
        return solicitudMapper.toDtoList(solicitudRepository.findByFarmaciaId(farmaciaId));
    }

    @Override
    public List<SolicitudResponseDto> listarPorEstado(String estado) {
        return solicitudMapper.toDtoList(solicitudRepository.findByEstado(estado));
    }

    @Override
    public SolicitudResponseDto buscarPorId(Long id) {
        return solicitudMapper.toDto(
                solicitudRepository.findById(id)
                        .orElseThrow(() -> new SolicitudNotFoundException(id))
        );
    }

    @Override
    public String consultarEstado(Long id) {
        return solicitudRepository.findById(id)
                .map(s -> s.getEstado())
                .orElseThrow(() -> new SolicitudNotFoundException(id));
    }

    @Override
    public void marcarEnProceso(Long id) {
        solicitudRepository.findById(id)
                .orElseThrow(() -> new SolicitudNotFoundException(id));
        try {
            llamarPrActualizarEstado(dataSource, id, "EN_PROCESO", null, "En preparación por almacenero");
            log.info("Solicitud id={} marcada EN_PROCESO", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20024")) {
                throw new EstadoInvalidoException("La solicitud no puede marcarse EN_PROCESO en su estado actual");
            }
            throw ex;
        }
    }

    @Override
    public void aprobar(Long id) {
        solicitudRepository.findById(id)
                .orElseThrow(() -> new SolicitudNotFoundException(id));
        try {
            llamarPrActualizarEstado(dataSource, id, "APROBADO_JEFE", null, "Aprobado por jefe de farmacia");
            log.info("Solicitud id={} aprobada por jefe", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20024")) {
                throw new EstadoInvalidoException("Solo se puede aprobar una solicitud en estado PENDIENTE");
            }
            throw ex;
        }
    }

    @Override
    public void cancelar(Long id) {
        solicitudRepository.findById(id)
                .orElseThrow(() -> new SolicitudNotFoundException(id));
        try {
            llamarPrActualizarEstado(dataSource, id, "CANCELADA", null, "Cancelada por el farmacéutico");
            log.info("Solicitud cancelada id={}", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20024")) {
                throw new EstadoInvalidoException("Solo se puede cancelar una solicitud en estado PENDIENTE");
            }
            throw ex;
        }
    }

    @Override
    public List<SolicitudResponseDto> listarActivas() {
        return solicitudMapper.toDtoList(
                solicitudRepository.findByEstadoIn(
                        List.of("APROBADO_JEFE", "EN_PROCESO", "PENDIENTE")));
    }

    @Override
    public void rechazar(Long id, String motivo) {
        solicitudRepository.findById(id)
                .orElseThrow(() -> new SolicitudNotFoundException(id));
        try {
            llamarPrActualizarEstado(dataSource, id, "RECHAZADA", null,
                    motivo != null ? motivo : "Rechazado por farmacia");
            log.info("Solicitud id={} rechazada. Motivo: {}", id, motivo);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20024")) {
                throw new EstadoInvalidoException("Solo se puede rechazar una solicitud en estado DESPACHADA");
            }
            throw ex;
        }
    }

    @Override
    public void confirmarEntrega(Long id) {
        solicitudRepository.findById(id)
                .orElseThrow(() -> new SolicitudNotFoundException(id));
        try {
            llamarPrActualizarEstado(dataSource, id, "ENTREGADA", null, "Recepción confirmada por farmacia");
            log.info("Solicitud id={} marcada ENTREGADA", id);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20024")) {
                throw new EstadoInvalidoException("Solo se puede confirmar entrega de una solicitud DESPACHADA");
            }
            throw ex;
        }
    }

    @Override
    public void procesarDespachoConfirmado(Map<String, Object> payload) {
        Long solicitudId = ((Number) payload.get("solicitudId")).longValue();
        String nuevoEstado;
        Long notaId = null;
        String observacion;

        if (payload.containsKey("error")) {
            nuevoEstado = "RECHAZADA";
            observacion = (String) payload.get("error");
        } else {
            nuevoEstado = "DESPACHADA";
            notaId = payload.get("notaId") != null
                    ? ((Number) payload.get("notaId")).longValue()
                    : null;
            observacion = "Nota de Salida: " + payload.get("nroNota");
        }

        try {
            llamarPrActualizarEstado(dataSource, solicitudId, nuevoEstado, notaId, observacion);
            log.info("Solicitud {} actualizada a estado={}", solicitudId, nuevoEstado);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20023")) {
                throw new SolicitudNotFoundException(solicitudId);
            }
            throw ex;
        }
    }

    Map<String, Object> llamarPrCrearSolicitud(DataSource ds, SolicitudRequestDto dto, Long farmaceuticoId) {
        String detallesJson;
        try {
            detallesJson = objectMapper.writeValueAsString(dto.getDetalles());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando detalles de solicitud", e);
        }

        SimpleJdbcCall call = new SimpleJdbcCall(ds)
                .withSchemaName("GP_FARMACIA")
                .withCatalogName("PKG_REQUERIMIENTO")
                .withProcedureName("PR_CREAR_SOLICITUD")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlParameter("p_farmacia_id",     Types.NUMERIC),
                    new SqlParameter("p_almacen_id",      Types.NUMERIC),
                    new SqlParameter("p_farmaceutico_id", Types.NUMERIC),
                    new SqlParameter("p_detalles_json",   Types.CLOB),
                    new SqlOutParameter("p_solicitud_id", Types.NUMERIC),
                    new SqlOutParameter("p_nro_solicitud", Types.VARCHAR)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("p_farmacia_id",     dto.getFarmaciaId());
        params.put("p_almacen_id",      dto.getAlmacenId());
        params.put("p_farmaceutico_id", farmaceuticoId);
        params.put("p_detalles_json",   detallesJson);

        try {
            return call.execute(params);
        } catch (Exception ex) {
            if (ex.getMessage() != null) {
                if (ex.getMessage().contains("ORA-20020")) {
                    throw new com.galenospro.farmacia.exception.FarmaciaNotFoundException(dto.getFarmaciaId());
                }
                if (ex.getMessage().contains("ORA-20022")) {
                    throw new SolicitudDuplicadaException(dto.getFarmaciaId());
                }
            }
            throw ex;
        }
    }

    void llamarPrActualizarEstado(DataSource ds, Long solicitudId,
                                   String nuevoEstado, Long notaId, String observacion) {
        SimpleJdbcCall call = new SimpleJdbcCall(ds)
                .withSchemaName("GP_FARMACIA")
                .withCatalogName("PKG_REQUERIMIENTO")
                .withProcedureName("PR_ACTUALIZAR_ESTADO")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlParameter("p_solicitud_id",   Types.NUMERIC),
                    new SqlParameter("p_nuevo_estado",   Types.VARCHAR),
                    new SqlParameter("p_nota_salida_id", Types.NUMERIC),
                    new SqlParameter("p_observacion",    Types.VARCHAR)
                );

        Map<String, Object> params = new HashMap<>();
        params.put("p_solicitud_id",   solicitudId);
        params.put("p_nuevo_estado",   nuevoEstado);
        params.put("p_nota_salida_id", notaId);
        params.put("p_observacion",    observacion);

        call.execute(params);
    }
}
