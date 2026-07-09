package com.galenospro.almacen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.almacen.config.SecurityConfig;
import com.galenospro.almacen.dto.EntradaStockRequestDto;
import com.galenospro.almacen.dto.StockResponseDto;
import com.galenospro.almacen.exception.GlobalExceptionHandler;
import com.galenospro.almacen.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class StockControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean StockService stockService;

    private StockResponseDto buildDto() {
        return StockResponseDto.builder()
                .id(1L).medicamentoId(10L).almacenId(1L)
                .lote("LOTE-001").cantidad(100).build();
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void GET_listarPorAlmacen_retorna_200() throws Exception {
        when(stockService.listarPorAlmacen(1L)).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/almacen/stock").param("almacenId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lote").value("LOTE-001"));
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void GET_listarPorMedicamento_retorna_200() throws Exception {
        when(stockService.listarPorMedicamento(10L, 1L)).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/almacen/stock/10").param("almacenId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidad").value(100));
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void GET_consultarDisponible_retorna_200() throws Exception {
        when(stockService.consultarDisponible(10L, 1L)).thenReturn(250);

        mockMvc.perform(get("/api/almacen/stock/10/disponible").param("almacenId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("250"));
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void POST_registrarEntrada_retorna_201() throws Exception {
        EntradaStockRequestDto request = new EntradaStockRequestDto();
        request.setMedicamentoId(10L);
        request.setAlmacenId(1L);
        request.setLote("LOTE-001");
        request.setCantidad(100);
        request.setFechaVencimiento(LocalDate.of(2026, 12, 31));
        request.setPrecioUnitario(new BigDecimal("5.50"));

        when(stockService.registrarEntrada(any())).thenReturn(buildDto());

        mockMvc.perform(post("/api/almacen/stock/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void POST_registrarEntrada_sin_auth_retorna_403() throws Exception {
        EntradaStockRequestDto request = new EntradaStockRequestDto();
        request.setMedicamentoId(10L);
        request.setAlmacenId(1L);
        request.setLote("LOTE-001");
        request.setCantidad(100);
        request.setFechaVencimiento(LocalDate.of(2026, 12, 31));
        request.setPrecioUnitario(new BigDecimal("5.50"));

        mockMvc.perform(post("/api/almacen/stock/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void POST_registrarEntrada_400_body_invalido() throws Exception {
        mockMvc.perform(post("/api/almacen/stock/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
