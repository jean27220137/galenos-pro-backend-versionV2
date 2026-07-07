package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.ProximoVencerDTO;
import com.galenospro.almacen.dto.StockCriticoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
}
