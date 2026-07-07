package com.galenospro.almacen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.almacen.config.SecurityConfig;
import com.galenospro.almacen.dto.MedicamentoRequestDto;
import com.galenospro.almacen.dto.MedicamentoResponseDto;
import com.galenospro.almacen.exception.GlobalExceptionHandler;
import com.galenospro.almacen.exception.MedicamentoNotFoundException;
import com.galenospro.almacen.service.MedicamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicamentoController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class MedicamentoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean MedicamentoService medicamentoService;

    private MedicamentoResponseDto buildDto() {
        return MedicamentoResponseDto.builder()
                .id(1L).nombre("Paracetamol 500mg").codigoSismed("SISMED-001").build();
    }

    @Test
    @WithMockUser
    void GET_listar_retorna_200() throws Exception {
        when(medicamentoService.listar()).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/almacen/medicamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Paracetamol 500mg"));
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_existente_retorna_200() throws Exception {
        when(medicamentoService.buscarPorId(1L)).thenReturn(buildDto());

        mockMvc.perform(get("/api/almacen/medicamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoSismed").value("SISMED-001"));
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_inexistente_retorna_404() throws Exception {
        when(medicamentoService.buscarPorId(99L)).thenThrow(new MedicamentoNotFoundException(99L));

        mockMvc.perform(get("/api/almacen/medicamentos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void POST_crear_medicamento_retorna_201() throws Exception {
        MedicamentoRequestDto request = new MedicamentoRequestDto();
        request.setNombre("Paracetamol 500mg");
        request.setCodigoSismed("SISMED-001");
        request.setPresentacion("Tableta");
        request.setConcentracion("500mg");
        request.setViaAdministracion("Oral");

        when(medicamentoService.crear(any())).thenReturn(buildDto());

        mockMvc.perform(post("/api/almacen/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
