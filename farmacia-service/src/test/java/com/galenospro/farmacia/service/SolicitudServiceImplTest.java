package com.galenospro.farmacia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.farmacia.dto.DetalleRequestDto;
import com.galenospro.farmacia.dto.SolicitudRequestDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;
import com.galenospro.farmacia.entity.SolicitudRequerimiento;
import com.galenospro.farmacia.exception.EstadoInvalidoException;
import com.galenospro.farmacia.exception.FarmaciaNotFoundException;
import com.galenospro.farmacia.exception.SolicitudDuplicadaException;
import com.galenospro.farmacia.exception.SolicitudNotFoundException;
import com.galenospro.farmacia.mapper.SolicitudMapper;
import com.galenospro.farmacia.messaging.FarmaciaPublisher;
import com.galenospro.farmacia.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceImplTest {

    @Mock SolicitudRepository solicitudRepository;
    @Mock SolicitudMapper solicitudMapper;
    @Mock ObjectMapper objectMapper;
    @Mock DataSource dataSource;
    @Mock FarmaciaPublisher farmaciaPublisher;

    @InjectMocks SolicitudServiceImpl solicitudService;

    private SolicitudRequerimiento solicitud;
    private SolicitudResponseDto solicitudDto;
    private SolicitudRequestDto requestDto;

    @BeforeEach
    void setUp() {
        solicitud = SolicitudRequerimiento.builder()
                .id(1L).nroSolicitud("SOL-000001")
                .farmaciaId(1L).almacenId(1L).farmaceuticoId(10L)
                .fechaSolicitud(LocalDate.now()).estado("PENDIENTE").build();

        solicitudDto = SolicitudResponseDto.builder()
                .id(1L).nroSolicitud("SOL-000001")
                .farmaciaId(1L).almacenId(1L).farmaceuticoId(10L)
                .estado("PENDIENTE").build();

        DetalleRequestDto detalle = new DetalleRequestDto();
        detalle.setMedicamentoId(10L);
        detalle.setCantidadSolicitada(50);

        requestDto = new SolicitudRequestDto();
        requestDto.setFarmaciaId(1L);
        requestDto.setAlmacenId(1L);
        requestDto.setDetalles(List.of(detalle));
    }

    // ── crear ────────────────────────────────────────────────────────────────

    @Test
    void crear_solicitud_exitoso_retorna_dto() {
        SolicitudServiceImpl spyService = spy(solicitudService);
        doReturn(Map.of("p_solicitud_id", 1L, "p_nro_solicitud", "SOL-000001"))
                .when(spyService).llamarPrCrearSolicitud(any(), any(), anyLong());
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudMapper.toDto(solicitud)).thenReturn(solicitudDto);
        doNothing().when(farmaciaPublisher).publicarSolicitudNueva(any(), any());

        SolicitudResponseDto result = spyService.crear(requestDto, 10L);

        assertThat(result.getNroSolicitud()).isEqualTo("SOL-000001");
        verify(farmaciaPublisher).publicarSolicitudNueva(any(), eq(requestDto));
    }

    @Test
    void crear_farmaciaId_nulo_lanza_excepcion() {
        requestDto.setFarmaciaId(null);

        assertThatThrownBy(() -> solicitudService.crear(requestDto, 10L))
                .isInstanceOf(EstadoInvalidoException.class)
                .hasMessageContaining("farmacia asignada");
    }

    @Test
    void crear_solicitud_duplicada_lanza_excepcion() {
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new SolicitudDuplicadaException(1L))
                .when(spyService).llamarPrCrearSolicitud(any(), any(), anyLong());

        assertThatThrownBy(() -> spyService.crear(requestDto, 10L))
                .isInstanceOf(SolicitudDuplicadaException.class);
    }

    // ── llamarPrCrearSolicitud (cobertura de ramas internas) ────────────────

    private static MockSettings returnsSelf() {
        return Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF);
    }

    @Test
    void llamarPrCrearSolicitud_exitoso_retorna_mapa() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        Map<String, Object> expected = Map.of("p_solicitud_id", 1L, "p_nro_solicitud", "SOL-000001");

        MockedConstruction.MockInitializer<SimpleJdbcCall> init =
                (mock, ctx) -> when(mock.execute(anyMap())).thenReturn(expected);
        try (MockedConstruction<SimpleJdbcCall> mocked =
                     Mockito.mockConstruction(SimpleJdbcCall.class, returnsSelf(), init)) {

            Map<String, Object> result = solicitudService.llamarPrCrearSolicitud(dataSource, requestDto, 10L);
            assertThat(result).containsKey("p_solicitud_id");
        }
    }

    @Test
    void llamarPrCrearSolicitud_json_error_lanza_RuntimeException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("json error") {});

        assertThatThrownBy(() -> solicitudService.llamarPrCrearSolicitud(dataSource, requestDto, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error serializando");
    }

    @Test
    void llamarPrCrearSolicitud_ORA20020_lanza_FarmaciaNotFoundException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        MockedConstruction.MockInitializer<SimpleJdbcCall> init =
                (mock, ctx) -> when(mock.execute(anyMap()))
                        .thenThrow(new RuntimeException("ORA-20020: farmacia no existe"));
        try (MockedConstruction<SimpleJdbcCall> mocked =
                     Mockito.mockConstruction(SimpleJdbcCall.class, returnsSelf(), init)) {

            assertThatThrownBy(() -> solicitudService.llamarPrCrearSolicitud(dataSource, requestDto, 10L))
                    .isInstanceOf(FarmaciaNotFoundException.class);
        }
    }

    @Test
    void llamarPrCrearSolicitud_ORA20022_lanza_SolicitudDuplicadaException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        MockedConstruction.MockInitializer<SimpleJdbcCall> init =
                (mock, ctx) -> when(mock.execute(anyMap()))
                        .thenThrow(new RuntimeException("ORA-20022: solicitud duplicada"));
        try (MockedConstruction<SimpleJdbcCall> mocked =
                     Mockito.mockConstruction(SimpleJdbcCall.class, returnsSelf(), init)) {

            assertThatThrownBy(() -> solicitudService.llamarPrCrearSolicitud(dataSource, requestDto, 10L))
                    .isInstanceOf(SolicitudDuplicadaException.class);
        }
    }

    @Test
    void llamarPrCrearSolicitud_error_generico_relanza() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        MockedConstruction.MockInitializer<SimpleJdbcCall> init =
                (mock, ctx) -> when(mock.execute(anyMap()))
                        .thenThrow(new RuntimeException("Conexión fallida"));
        try (MockedConstruction<SimpleJdbcCall> mocked =
                     Mockito.mockConstruction(SimpleJdbcCall.class, returnsSelf(), init)) {

            assertThatThrownBy(() -> solicitudService.llamarPrCrearSolicitud(dataSource, requestDto, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Conexión fallida");
        }
    }

    // ── llamarPrActualizarEstado (cobertura de líneas internas) ─────────────

    @Test
    void llamarPrActualizarEstado_exitoso() {
        MockedConstruction.MockInitializer<SimpleJdbcCall> init =
                (mock, ctx) -> when(mock.execute(anyMap())).thenReturn(Map.of());
        try (MockedConstruction<SimpleJdbcCall> mocked =
                     Mockito.mockConstruction(SimpleJdbcCall.class, returnsSelf(), init)) {

            assertThatNoException().isThrownBy(() ->
                    solicitudService.llamarPrActualizarEstado(dataSource, 1L, "ENTREGADA", null, "Recepción confirmada"));
        }
    }

    // ── listar ───────────────────────────────────────────────────────────────

    @Test
    void listarPorFarmacia_retorna_lista() {
        when(solicitudRepository.findByFarmaciaId(1L)).thenReturn(List.of(solicitud));
        when(solicitudMapper.toDtoList(List.of(solicitud))).thenReturn(List.of(solicitudDto));

        List<SolicitudResponseDto> result = solicitudService.listarPorFarmacia(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarPorEstado_retorna_lista() {
        when(solicitudRepository.findByEstado("PENDIENTE")).thenReturn(List.of(solicitud));
        when(solicitudMapper.toDtoList(List.of(solicitud))).thenReturn(List.of(solicitudDto));

        List<SolicitudResponseDto> result = solicitudService.listarPorEstado("PENDIENTE");

        assertThat(result).hasSize(1);
    }

    @Test
    void listarActivas_retorna_lista() {
        when(solicitudRepository.findByEstadoIn(anyList())).thenReturn(List.of(solicitud));
        when(solicitudMapper.toDtoList(List.of(solicitud))).thenReturn(List.of(solicitudDto));

        List<SolicitudResponseDto> result = solicitudService.listarActivas();

        assertThat(result).hasSize(1);
    }

    // ── buscarPorId ──────────────────────────────────────────────────────────

    @Test
    void buscarPorId_existente_retorna_dto() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudMapper.toDto(solicitud)).thenReturn(solicitudDto);

        SolicitudResponseDto result = solicitudService.buscarPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_inexistente_lanza_excepcion() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.buscarPorId(99L))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    // ── consultarEstado ──────────────────────────────────────────────────────

    @Test
    void consultarEstado_retorna_estado() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        String estado = solicitudService.consultarEstado(1L);

        assertThat(estado).isEqualTo("PENDIENTE");
    }

    @Test
    void consultarEstado_inexistente_lanza_excepcion() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.consultarEstado(99L))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    // ── marcarEnProceso ──────────────────────────────────────────────────────

    @Test
    void marcarEnProceso_solicitud_pendiente_exitoso() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.marcarEnProceso(1L);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "EN_PROCESO", null, "En preparación por almacenero");
    }

    @Test
    void marcarEnProceso_solicitud_inexistente_lanza_excepcion() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.marcarEnProceso(99L))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    @Test
    void marcarEnProceso_estado_invalido_lanza_excepcion() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("ORA-20024: transición inválida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.marcarEnProceso(1L))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void marcarEnProceso_error_generico_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("Conexión fallida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.marcarEnProceso(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conexión fallida");
    }

    // ── aprobar ──────────────────────────────────────────────────────────────

    @Test
    void aprobar_solicitud_exitoso() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.aprobar(1L);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "APROBADO_JEFE", null, "Aprobado por jefe de farmacia");
    }

    @Test
    void aprobar_solicitud_inexistente_lanza_excepcion() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.aprobar(99L))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    @Test
    void aprobar_estado_invalido_lanza_excepcion() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("ORA-20024: transición inválida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.aprobar(1L))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void aprobar_error_generico_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("Conexión fallida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.aprobar(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conexión fallida");
    }

    // ── cancelar ─────────────────────────────────────────────────────────────

    @Test
    void cancelar_solicitud_pendiente_exitoso() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.cancelar(1L);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "CANCELADA", null, "Cancelada por el farmacéutico");
    }

    @Test
    void cancelar_solicitud_inexistente_lanza_excepcion() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.cancelar(99L))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    @Test
    void cancelar_estado_invalido_lanza_excepcion() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("ORA-20024: transición inválida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.cancelar(1L))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void cancelar_error_generico_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("Conexión fallida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.cancelar(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conexión fallida");
    }

    // ── rechazar ─────────────────────────────────────────────────────────────

    @Test
    void rechazar_con_motivo_exitoso() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.rechazar(1L, "Medicamentos incompletos");

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "RECHAZADA", null, "Medicamentos incompletos");
    }

    @Test
    void rechazar_sin_motivo_usa_default() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.rechazar(1L, null);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "RECHAZADA", null, "Rechazado por farmacia");
    }

    @Test
    void rechazar_solicitud_inexistente_lanza_excepcion() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.rechazar(99L, "motivo"))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    @Test
    void rechazar_estado_invalido_lanza_excepcion() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("ORA-20024: transición inválida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.rechazar(1L, "motivo"))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void rechazar_error_generico_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("Conexión fallida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.rechazar(1L, "motivo"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conexión fallida");
    }

    // ── confirmarEntrega ─────────────────────────────────────────────────────

    @Test
    void confirmarEntrega_exitoso() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.confirmarEntrega(1L);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "ENTREGADA", null, "Recepción confirmada por farmacia");
    }

    @Test
    void confirmarEntrega_solicitud_inexistente_lanza_excepcion() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudService.confirmarEntrega(99L))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    @Test
    void confirmarEntrega_estado_invalido_lanza_excepcion() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("ORA-20024: transición inválida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.confirmarEntrega(1L))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void confirmarEntrega_error_generico_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("Conexión fallida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.confirmarEntrega(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conexión fallida");
    }

    // ── procesarDespachoConfirmado ───────────────────────────────────────────

    @Test
    void procesarDespachoConfirmado_exitoso_actualiza_estado() {
        Map<String, Object> payload = Map.of(
                "solicitudId", 1L,
                "notaId",      5L,
                "nroNota",     "NS-000001",
                "estado",      "GENERADA"
        );
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.procesarDespachoConfirmado(payload);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "DESPACHADA", 5L, "Nota de Salida: NS-000001");
    }

    @Test
    void procesarDespachoConfirmado_sin_notaId_usa_null() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("solicitudId", 1L);
        payload.put("notaId",      null);
        payload.put("nroNota",     "NS-000001");

        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.procesarDespachoConfirmado(payload);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "DESPACHADA", null, "Nota de Salida: NS-000001");
    }

    @Test
    void procesarDespachoConfirmado_con_error_marca_rechazada() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("solicitudId", 1L);
        payload.put("error", "Stock insuficiente");

        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.procesarDespachoConfirmado(payload);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "RECHAZADA", null, "Stock insuficiente");
    }

    @Test
    void procesarDespachoConfirmado_con_ORA20023_lanza_SolicitudNotFoundException() {
        Map<String, Object> payload = Map.of(
                "solicitudId", 1L,
                "notaId",      5L,
                "nroNota",     "NS-000001",
                "estado",      "GENERADA"
        );
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("ORA-20023: solicitud no encontrada"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.procesarDespachoConfirmado(payload))
                .isInstanceOf(SolicitudNotFoundException.class);
    }

    @Test
    void procesarDespachoConfirmado_error_generico_relanza() {
        Map<String, Object> payload = Map.of(
                "solicitudId", 1L,
                "notaId",      5L,
                "nroNota",     "NS-000001",
                "estado",      "GENERADA"
        );
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException("Conexión fallida"))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.procesarDespachoConfirmado(payload))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conexión fallida");
    }

    // ── null-message branch tests ─────────────────────────────────────────────

    @Test
    void llamarPrCrearSolicitud_excepcion_mensaje_nulo_relanza() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        MockedConstruction.MockInitializer<SimpleJdbcCall> init =
                (mock, ctx) -> when(mock.execute(anyMap()))
                        .thenThrow(new RuntimeException((String) null));
        try (MockedConstruction<SimpleJdbcCall> mocked =
                     Mockito.mockConstruction(SimpleJdbcCall.class, returnsSelf(), init)) {
            assertThatThrownBy(() -> solicitudService.llamarPrCrearSolicitud(dataSource, requestDto, 10L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void marcarEnProceso_excepcion_mensaje_nulo_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException((String) null))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.marcarEnProceso(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void aprobar_excepcion_mensaje_nulo_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException((String) null))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.aprobar(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void cancelar_excepcion_mensaje_nulo_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException((String) null))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.cancelar(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void rechazar_excepcion_mensaje_nulo_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException((String) null))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.rechazar(1L, "motivo"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void confirmarEntrega_excepcion_mensaje_nulo_relanza() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException((String) null))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.confirmarEntrega(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void procesarDespachoConfirmado_excepcion_mensaje_nulo_relanza() {
        Map<String, Object> payload = Map.of(
                "solicitudId", 1L,
                "notaId",      5L,
                "nroNota",     "NS-000001",
                "estado",      "GENERADA"
        );
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new RuntimeException((String) null))
                .when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        assertThatThrownBy(() -> spyService.procesarDespachoConfirmado(payload))
                .isInstanceOf(RuntimeException.class);
    }
}
