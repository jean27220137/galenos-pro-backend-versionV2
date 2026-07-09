package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.ProximoVencerDTO;
import com.galenospro.almacen.dto.StockCriticoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    DataSource dataSource;

    @Spy
    @InjectMocks
    DashboardServiceImpl service;

    // ── getStockCritico ───────────────────────────────────────────────────────

    @Test
    void getStockCritico_retorna_lista_con_items() {
        StockCriticoDTO item = StockCriticoDTO.builder()
                .medicamentoNombre("Paracetamol 500mg").codigoSismed("PA-001")
                .presentacion("Tableta").cantidadActual(5).stockMinimo(20)
                .build();
        doReturn(List.of(item)).when(service).ejecutarFnDashboardCritico();

        List<StockCriticoDTO> resultado = service.getStockCritico();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMedicamentoNombre()).isEqualTo("Paracetamol 500mg");
        assertThat(resultado.get(0).getCantidadActual()).isEqualTo(5);
        verify(service).ejecutarFnDashboardCritico();
    }

    @Test
    void getStockCritico_retorna_lista_vacia_cuando_no_hay_criticos() {
        doReturn(Collections.emptyList()).when(service).ejecutarFnDashboardCritico();

        List<StockCriticoDTO> resultado = service.getStockCritico();

        assertThat(resultado).isEmpty();
    }

    // ── getProximosAVencer ────────────────────────────────────────────────────

    @Test
    void getProximosAVencer_retorna_lista_con_items() {
        ProximoVencerDTO item = ProximoVencerDTO.builder()
                .medicamentoNombre("Amoxicilina 500mg").codigoSismed("AM-002")
                .lote("LOTE-A1").fechaVencimiento(LocalDate.now().plusDays(30))
                .diasRestantes(30).cantidad(100).almacenNombre("Almacén Central")
                .build();
        doReturn(List.of(item)).when(service).ejecutarFnProximosVencer(90);

        List<ProximoVencerDTO> resultado = service.getProximosAVencer(90);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDiasRestantes()).isEqualTo(30);
        assertThat(resultado.get(0).getLote()).isEqualTo("LOTE-A1");
        verify(service).ejecutarFnProximosVencer(90);
    }

    @Test
    void getProximosAVencer_retorna_lista_vacia_cuando_no_hay_vencimientos() {
        doReturn(Collections.emptyList()).when(service).ejecutarFnProximosVencer(90);

        List<ProximoVencerDTO> resultado = service.getProximosAVencer(90);

        assertThat(resultado).isEmpty();
    }

    @Test
    void getProximosAVencer_usa_parametro_dias_personalizado() {
        doReturn(Collections.emptyList()).when(service).ejecutarFnProximosVencer(30);

        service.getProximosAVencer(30);

        verify(service).ejecutarFnProximosVencer(30);
    }

    // ── ejecutarFnDashboardCritico — directos ─────────────────────────────────

    @Test
    void ejecutarFnDashboardCritico_retorna_lista_con_datos() {
        StockCriticoDTO item = StockCriticoDTO.builder()
                .medicamentoNombre("Paracetamol 500mg").codigoSismed("PA-001")
                .presentacion("Tableta").cantidadActual(5).stockMinimo(20).build();

        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doReturn(Map.of("return", List.of(item))).when(mock).execute())) {
            List<StockCriticoDTO> result = service.ejecutarFnDashboardCritico();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMedicamentoNombre()).isEqualTo("Paracetamol 500mg");
        }
    }

    @Test
    void ejecutarFnDashboardCritico_retorna_vacio_cuando_no_hay_resultado() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doReturn(Collections.emptyMap()).when(mock).execute())) {
            List<StockCriticoDTO> result = service.ejecutarFnDashboardCritico();
            assertThat(result).isEmpty();
        }
    }

    // ── ejecutarFnProximosVencer — directos ───────────────────────────────────

    @Test
    void ejecutarFnProximosVencer_retorna_lista_con_datos() {
        ProximoVencerDTO item = ProximoVencerDTO.builder()
                .medicamentoNombre("Amoxicilina 500mg").codigoSismed("AM-002")
                .lote("LOTE-A1").fechaVencimiento(LocalDate.now().plusDays(30))
                .diasRestantes(30).cantidad(100).almacenNombre("Almacén Central").build();

        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doReturn(Map.of("return", List.of(item))).when(mock).execute(any(SqlParameterSource.class)))) {
            List<ProximoVencerDTO> result = service.ejecutarFnProximosVencer(90);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLote()).isEqualTo("LOTE-A1");
        }
    }

    @Test
    void ejecutarFnProximosVencer_retorna_vacio_cuando_no_hay_resultado() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockConstruction(
                SimpleJdbcCall.class,
                withSettings().defaultAnswer(Answers.RETURNS_SELF),
                (mock, ctx) -> doReturn(Collections.emptyMap()).when(mock).execute(any(SqlParameterSource.class)))) {
            List<ProximoVencerDTO> result = service.ejecutarFnProximosVencer(30);
            assertThat(result).isEmpty();
        }
    }
}
