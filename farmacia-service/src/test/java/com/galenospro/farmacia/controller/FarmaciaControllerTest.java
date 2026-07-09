package com.galenospro.farmacia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.farmacia.config.SecurityConfig;
import com.galenospro.farmacia.dto.FarmaciaRequestDto;
import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.exception.FarmaciaNotFoundException;
import com.galenospro.farmacia.exception.GlobalExceptionHandler;
import com.galenospro.farmacia.service.FarmaciaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private FarmaciaRequestDto buildRequest() {
        return FarmaciaRequestDto.builder()
                .codigo("FAR-001").nombre("Farmacia Central").tipo("CONSULTA_EXTERNA").build();
    }

    // ── GET /farmacias ───────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void GET_listar_retorna_200() throws Exception {
        when(farmaciaService.listar()).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/farmacia/farmacias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("FAR-001"));
    }

    // ── GET /farmacias/todas ─────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void GET_listarTodas_admin_retorna_200() throws Exception {
        when(farmaciaService.listarTodas()).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/farmacia/farmacias/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Farmacia Central"));
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void GET_listarTodas_sin_admin_retorna_403() throws Exception {
        mockMvc.perform(get("/api/farmacia/farmacias/todas"))
                .andExpect(status().isForbidden());
    }

    // ── GET /farmacias/{id} ──────────────────────────────────────────────────

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

    // ── POST /farmacias ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void POST_crear_retorna_201() throws Exception {
        when(farmaciaService.crear(any())).thenReturn(buildDto());

        mockMvc.perform(post("/api/farmacia/farmacias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("FAR-001"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void POST_crear_codigo_invalido_retorna_400() throws Exception {
        FarmaciaRequestDto badRequest = FarmaciaRequestDto.builder()
                .codigo("INVALIDO").nombre("Farmacia").tipo("CONSULTA_EXTERNA").build();

        mockMvc.perform(post("/api/farmacia/farmacias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void POST_crear_sin_admin_retorna_403() throws Exception {
        mockMvc.perform(post("/api/farmacia/farmacias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void POST_crear_codigo_duplicado_retorna_409() throws Exception {
        when(farmaciaService.crear(any()))
                .thenThrow(new IllegalArgumentException("Ya existe una farmacia con el código FAR-001"));

        mockMvc.perform(post("/api/farmacia/farmacias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isConflict());
    }

    // ── PUT /farmacias/{id} ──────────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_actualizar_retorna_200() throws Exception {
        when(farmaciaService.actualizar(eq(1L), any())).thenReturn(buildDto());

        mockMvc.perform(put("/api/farmacia/farmacias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("FAR-001"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_actualizar_inexistente_retorna_404() throws Exception {
        when(farmaciaService.actualizar(eq(99L), any()))
                .thenThrow(new FarmaciaNotFoundException(99L));

        mockMvc.perform(put("/api/farmacia/farmacias/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void PUT_actualizar_sin_admin_retorna_403() throws Exception {
        mockMvc.perform(put("/api/farmacia/farmacias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_actualizar_codigo_invalido_retorna_400() throws Exception {
        FarmaciaRequestDto badRequest = FarmaciaRequestDto.builder()
                .codigo("INVALIDO").nombre("Farmacia").tipo("CONSULTA_EXTERNA").build();

        mockMvc.perform(put("/api/farmacia/farmacias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /farmacias/{id}/desactivar ───────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_desactivar_retorna_204() throws Exception {
        doNothing().when(farmaciaService).desactivar(1L);

        mockMvc.perform(put("/api/farmacia/farmacias/1/desactivar"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_desactivar_inexistente_retorna_404() throws Exception {
        doThrow(new FarmaciaNotFoundException(99L)).when(farmaciaService).desactivar(99L);

        mockMvc.perform(put("/api/farmacia/farmacias/99/desactivar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void PUT_desactivar_sin_admin_retorna_403() throws Exception {
        mockMvc.perform(put("/api/farmacia/farmacias/1/desactivar"))
                .andExpect(status().isForbidden());
    }

    // ── PUT /farmacias/{id}/activar ──────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_activar_retorna_204() throws Exception {
        doNothing().when(farmaciaService).activar(1L);

        mockMvc.perform(put("/api/farmacia/farmacias/1/activar"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_activar_inexistente_retorna_404() throws Exception {
        doThrow(new FarmaciaNotFoundException(99L)).when(farmaciaService).activar(99L);

        mockMvc.perform(put("/api/farmacia/farmacias/99/activar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "JEFE_FARMACIA")
    void PUT_activar_sin_admin_retorna_403() throws Exception {
        mockMvc.perform(put("/api/farmacia/farmacias/1/activar"))
                .andExpect(status().isForbidden());
    }
}
