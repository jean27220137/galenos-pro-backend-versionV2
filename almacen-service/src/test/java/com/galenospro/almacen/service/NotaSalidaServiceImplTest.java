package com.galenospro.almacen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.entity.NotaSalida;
import com.galenospro.almacen.exception.NotaSalidaNotFoundException;
import com.galenospro.almacen.mapper.NotaSalidaMapper;
import com.galenospro.almacen.repository.NotaSalidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaSalidaServiceImplTest {

    @Mock NotaSalidaRepository notaSalidaRepository;
    @Mock NotaSalidaMapper notaSalidaMapper;
    @Mock ObjectMapper objectMapper;
    @Mock DataSource dataSource;

    @InjectMocks NotaSalidaServiceImpl notaSalidaService;

    private NotaSalida nota;
    private NotaSalidaResponseDto notaDto;
    private DespachoSolicitudDto despachoDto;

    @BeforeEach
    void setUp() {
        nota = new NotaSalida();
        nota.setId(1L);
        nota.setNroNotaSalida("NS-000001");
        nota.setEstado("PENDIENTE");

        notaDto = NotaSalidaResponseDto.builder()
                .id(1L).nroNotaSalida("NS-000001").estado("PENDIENTE").build();

        despachoDto = new DespachoSolicitudDto();
        despachoDto.setSolicitudId(100L);
        despachoDto.setAlmacenId(1L);
        despachoDto.setAlmacenDestinoId(2L);
        despachoDto.setDespachadorId(10L);
        despachoDto.setFarmaciaId(3L);
        despachoDto.setDetalles(List.of());
    }

    @Test
    void buscarPorId_existente_retorna_dto() {
        when(notaSalidaRepository.findById(1L)).thenReturn(Optional.of(nota));
        when(notaSalidaMapper.toDto(nota)).thenReturn(notaDto);

        NotaSalidaResponseDto result = notaSalidaService.buscarPorId(1L);

        assertThat(result.getNroNotaSalida()).isEqualTo("NS-000001");
    }

    @Test
    void buscarPorId_inexistente_lanza_excepcion() {
        when(notaSalidaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notaSalidaService.buscarPorId(99L))
                .isInstanceOf(NotaSalidaNotFoundException.class);
    }

    @Test
    void listarPorAlmacen_retorna_lista() {
        when(notaSalidaRepository.findByAlmacenOrigenId(1L)).thenReturn(List.of(nota));
        when(notaSalidaMapper.toDtoList(List.of(nota))).thenReturn(List.of(notaDto));

        List<NotaSalidaResponseDto> result = notaSalidaService.listarPorAlmacen(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void despacharSolicitud_exitoso_retorna_dto() {
        NotaSalidaServiceImpl spyService = spy(notaSalidaService);
        doReturn(Map.of("p_nota_id", 1L, "p_nro_nota", "NS-000001"))
                .when(spyService).llamarPrDespacharSolicitud(any(), any());
        when(notaSalidaRepository.findById(1L)).thenReturn(Optional.of(nota));
        when(notaSalidaMapper.toDto(nota)).thenReturn(notaDto);

        NotaSalidaResponseDto result = spyService.despacharSolicitud(despachoDto);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void confirmarEntrega_llama_procedimiento() {
        NotaSalidaServiceImpl spyService = spy(notaSalidaService);
        doNothing().when(spyService).llamarPrConfirmarEntrega(any(), anyLong());

        spyService.confirmarEntrega(1L);

        verify(spyService).llamarPrConfirmarEntrega(dataSource, 1L);
    }

    // ── llamarPrDespacharSolicitud — branches de error ────────────────────────

    @Test
    void llamarPrDespacharSolicitud_ORA20010_lanza_NotaSalidaNotFoundException()
            throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doThrow(new RuntimeException("ORA-20010: solicitud no valida"))
                        .when(mock).execute(any(Map.class)))) {
            assertThatThrownBy(() ->
                    notaSalidaService.llamarPrDespacharSolicitud(dataSource, despachoDto))
                    .isInstanceOf(NotaSalidaNotFoundException.class);
        }
    }

    @Test
    void llamarPrDespacharSolicitud_excepcion_generica_se_propaga()
            throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doThrow(new RuntimeException("DB connection error"))
                        .when(mock).execute(any(Map.class)))) {
            assertThatThrownBy(() ->
                    notaSalidaService.llamarPrDespacharSolicitud(dataSource, despachoDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB connection error");
        }
    }

    @Test
    void llamarPrDespacharSolicitud_JsonProcessingException_lanza_RuntimeException()
            throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("parse error") {});

        assertThatThrownBy(() ->
                notaSalidaService.llamarPrDespacharSolicitud(dataSource, despachoDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error serializando");
    }

    // ── llamarPrConfirmarEntrega — branches de error ──────────────────────────

    @Test
    void llamarPrConfirmarEntrega_ORA20011_lanza_NotaSalidaNotFoundException() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doThrow(new RuntimeException("ORA-20011: nota ya entregada"))
                        .when(mock).execute(any(Map.class)))) {
            assertThatThrownBy(() ->
                    notaSalidaService.llamarPrConfirmarEntrega(dataSource, 1L))
                    .isInstanceOf(NotaSalidaNotFoundException.class);
        }
    }

    @Test
    void llamarPrConfirmarEntrega_excepcion_generica_se_propaga() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doThrow(new RuntimeException("Timeout error"))
                        .when(mock).execute(any(Map.class)))) {
            assertThatThrownBy(() ->
                    notaSalidaService.llamarPrConfirmarEntrega(dataSource, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Timeout error");
        }
    }

    @Test
    void llamarPrDespacharSolicitud_exitoso_retorna_mapa() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doReturn(Map.of("p_nota_id", 1L, "p_nro_nota", "NS-000001", "p_nro_movimiento", "MOV-001"))
                        .when(mock).execute(any(Map.class)))) {
            Map<String, Object> result = notaSalidaService.llamarPrDespacharSolicitud(dataSource, despachoDto);
            assertThat(result).containsKey("p_nota_id");
        }
    }

    @Test
    void llamarPrConfirmarEntrega_exitoso_no_lanza_excepcion() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF))) {
            assertThatNoException().isThrownBy(() -> notaSalidaService.llamarPrConfirmarEntrega(dataSource, 1L));
        }
    }

    @Test
    void llamarPrDespacharSolicitud_excepcion_mensaje_nulo_relanza() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doThrow(new RuntimeException((String) null))
                        .when(mock).execute(any(Map.class)))) {
            assertThatThrownBy(() ->
                    notaSalidaService.llamarPrDespacharSolicitud(dataSource, despachoDto))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void llamarPrConfirmarEntrega_excepcion_mensaje_nulo_relanza() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doThrow(new RuntimeException((String) null))
                        .when(mock).execute(any(Map.class)))) {
            assertThatThrownBy(() ->
                    notaSalidaService.llamarPrConfirmarEntrega(dataSource, 1L))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
