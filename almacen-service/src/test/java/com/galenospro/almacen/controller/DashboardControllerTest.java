package com.galenospro.almacen.controller;

import com.galenospro.almacen.config.SecurityConfig;
import com.galenospro.almacen.dto.ProximoVencerDTO;
import com.galenospro.almacen.dto.StockCriticoDTO;
import com.galenospro.almacen.exception.GlobalExceptionHandler;
import com.galenospro.almacen.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DashboardService dashboardService;

    // ── /api/almacen/dashboard/stock-critico ──────────────────────────────────

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void GET_stockCritico_retorna_200_con_lista() throws Exception {
        StockCriticoDTO item = StockCriticoDTO.builder()
                .medicamentoNombre("Paracetamol 500mg").codigoSismed("PA-001")
                .presentacion("Tableta").cantidadActual(5).stockMinimo(20)
                .build();
        when(dashboardService.getStockCritico()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/almacen/dashboard/stock-critico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].medicamentoNombre").value("Paracetamol 500mg"))
                .andExpect(jsonPath("$[0].cantidadActual").value(5))
                .andExpect(jsonPath("$[0].stockMinimo").value(20));
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void GET_stockCritico_retorna_200_lista_vacia() throws Exception {
        when(dashboardService.getStockCritico()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/almacen/dashboard/stock-critico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void GET_stockCritico_retorna_403_cuando_rol_no_autorizado() throws Exception {
        mockMvc.perform(get("/api/almacen/dashboard/stock-critico"))
                .andExpect(status().isForbidden());
    }

    // ── /api/almacen/dashboard/proximos-vencer ────────────────────────────────

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void GET_proximosVencer_retorna_200_con_param_default() throws Exception {
        ProximoVencerDTO item = ProximoVencerDTO.builder()
                .medicamentoNombre("Amoxicilina 500mg").codigoSismed("AM-002")
                .lote("LOTE-A1").fechaVencimiento(LocalDate.now().plusDays(30))
                .diasRestantes(30).cantidad(100).almacenNombre("Almacén Central")
                .build();
        when(dashboardService.getProximosAVencer(90)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/almacen/dashboard/proximos-vencer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lote").value("LOTE-A1"))
                .andExpect(jsonPath("$[0].diasRestantes").value(30));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void GET_proximosVencer_retorna_200_con_param_personalizado() throws Exception {
        when(dashboardService.getProximosAVencer(30)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/almacen/dashboard/proximos-vencer").param("dias", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(authorities = "JEFE_FARMACIA")
    void GET_proximosVencer_retorna_403_cuando_rol_no_autorizado() throws Exception {
        mockMvc.perform(get("/api/almacen/dashboard/proximos-vencer"))
                .andExpect(status().isForbidden());
    }
}
