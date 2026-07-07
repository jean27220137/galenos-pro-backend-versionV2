package com.galenospro.farmacia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.farmacia.config.SecurityConfig;
import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.exception.FarmaciaNotFoundException;
import com.galenospro.farmacia.exception.GlobalExceptionHandler;
import com.galenospro.farmacia.service.FarmaciaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FarmaciaController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class FarmaciaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean FarmaciaService farmaciaService;

    private FarmaciaResponseDto buildDto() {
        return FarmaciaResponseDto.builder()
                .id(1L).codigo("FAR-001").nombre("Farmacia Central")
                .area("Consulta Externa").tipo("CONSULTA_EXTERNA").build();
    }

    @Test
    @WithMockUser
    void GET_listar_retorna_200() throws Exception {
        when(farmaciaService.listar()).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/farmacia/farmacias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("FAR-001"));
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_existente_retorna_200() throws Exception {
        when(farmaciaService.buscarPorId(1L)).thenReturn(buildDto());

        mockMvc.perform(get("/api/farmacia/farmacias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Farmacia Central"));
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_inexistente_retorna_404() throws Exception {
        when(farmaciaService.buscarPorId(99L)).thenThrow(new FarmaciaNotFoundException(99L));

        mockMvc.perform(get("/api/farmacia/farmacias/99"))
                .andExpect(status().isNotFound());
    }
}
