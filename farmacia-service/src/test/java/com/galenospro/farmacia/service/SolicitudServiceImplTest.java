package com.galenospro.farmacia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.farmacia.dto.DetalleRequestDto;
import com.galenospro.farmacia.dto.SolicitudRequestDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;
import com.galenospro.farmacia.entity.SolicitudRequerimiento;
import com.galenospro.farmacia.exception.EstadoInvalidoException;
import com.galenospro.farmacia.exception.SolicitudDuplicadaException;
import com.galenospro.farmacia.exception.SolicitudNotFoundException;
import com.galenospro.farmacia.mapper.SolicitudMapper;
import com.galenospro.farmacia.messaging.FarmaciaPublisher;
import com.galenospro.farmacia.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.time.LocalDate;
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
    void crear_solicitud_duplicada_lanza_excepcion() {
        SolicitudServiceImpl spyService = spy(solicitudService);
        doThrow(new SolicitudDuplicadaException(1L))
                .when(spyService).llamarPrCrearSolicitud(any(), any(), anyLong());

        assertThatThrownBy(() -> spyService.crear(requestDto, 10L))
                .isInstanceOf(SolicitudDuplicadaException.class);
    }

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

    @Test
    void consultarEstado_retorna_estado() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        String estado = solicitudService.consultarEstado(1L);

        assertThat(estado).isEqualTo("PENDIENTE");
    }

    @Test
    void cancelar_solicitud_pendiente_exitoso() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.cancelar(1L);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "CANCELADA", null, "Cancelada por el farmacéutico");
    }

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
    void marcarEnProceso_solicitud_pendiente_exitoso() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.marcarEnProceso(1L);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "EN_PROCESO", null, "Tomada por almacenero");
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
    void procesarDespachoConfirmado_con_error_marca_rechazada() {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("solicitudId", 1L);
        payload.put("error", "Stock insuficiente");

        SolicitudServiceImpl spyService = spy(solicitudService);
        doNothing().when(spyService).llamarPrActualizarEstado(any(), anyLong(), any(), any(), any());

        spyService.procesarDespachoConfirmado(payload);

        verify(spyService).llamarPrActualizarEstado(dataSource, 1L, "RECHAZADA", null, "Stock insuficiente");
    }
}
