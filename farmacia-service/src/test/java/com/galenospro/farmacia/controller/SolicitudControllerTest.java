package com.galenospro.farmacia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.farmacia.config.SecurityConfig;
import com.galenospro.farmacia.dto.DetalleRequestDto;
import com.galenospro.farmacia.dto.SolicitudRequestDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;
import com.galenospro.farmacia.exception.GlobalExceptionHandler;
import com.galenospro.farmacia.exception.SolicitudNotFoundException;
import com.galenospro.farmacia.service.SolicitudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SolicitudController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class SolicitudControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean SolicitudService solicitudService;

    private SolicitudResponseDto buildDto() {
        return SolicitudResponseDto.builder()
                .id(1L).nroSolicitud("SOL-000001")
                .farmaciaId(1L).almacenId(1L).farmaceuticoId(10L)
                .fechaSolicitud(LocalDate.now()).estado("PENDIENTE").build();
    }

    private SolicitudRequestDto buildRequest() {
        DetalleRequestDto detalle = new DetalleRequestDto();
        detalle.setMedicamentoId(10L);
        detalle.setCantidadSolicitada(50);

        SolicitudRequestDto req = new SolicitudRequestDto();
        req.setFarmaciaId(1L);
        req.setAlmacenId(1L);
        req.setDetalles(List.of(detalle));
        return req;
    }

    @Test
    @WithMockUser(authorities = "JEFE_FARMACIA")
    void POST_crear_solicitud_retorna_201() throws Exception {
        when(solicitudService.crear(any(), eq(10L))).thenReturn(buildDto());

        mockMvc.perform(post("/api/farmacia/solicitudes")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nroSolicitud").value("SOL-000001"));
    }

    @Test
    @WithMockUser
    void GET_listarPorFarmacia_retorna_200() throws Exception {
        when(solicitudService.listarPorFarmacia(1L)).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/farmacia/solicitudes").param("farmaciaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser
    void GET_listarPorEstado_retorna_200() throws Exception {
        when(solicitudService.listarPorEstado("EN_PROCESO")).thenReturn(List.of(buildDto()));

        mockMvc.perform(get("/api/farmacia/solicitudes").param("estado", "EN_PROCESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nroSolicitud").value("SOL-000001"));
    }

    @Test
    @WithMockUser
    void GET_listar_sin_params_retorna_lista_vacia() throws Exception {
        mockMvc.perform(get("/api/farmacia/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_existente_retorna_200() throws Exception {
        when(solicitudService.buscarPorId(1L)).thenReturn(buildDto());

        mockMvc.perform(get("/api/farmacia/solicitudes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void GET_consultarEstado_retorna_200() throws Exception {
        when(solicitudService.consultarEstado(1L)).thenReturn("PENDIENTE");

        mockMvc.perform(get("/api/farmacia/solicitudes/1/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(authorities = "JEFE_FARMACIA")
    void PUT_cancelar_retorna_204() throws Exception {
        doNothing().when(solicitudService).cancelar(1L);

        mockMvc.perform(put("/api/farmacia/solicitudes/1/cancelar"))
                .andExpect(status().isNoContent());
    }

    @Test
    void POST_crear_sin_auth_retorna_403() throws Exception {
        mockMvc.perform(post("/api/farmacia/solicitudes")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void PUT_en_proceso_retorna_204() throws Exception {
        doNothing().when(solicitudService).marcarEnProceso(1L);

        mockMvc.perform(put("/api/farmacia/solicitudes/1/en-proceso"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void PUT_en_proceso_sin_rol_almacenero_retorna_403() throws Exception {
        mockMvc.perform(put("/api/farmacia/solicitudes/1/en-proceso"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_inexistente_retorna_404() throws Exception {
        when(solicitudService.buscarPorId(99L)).thenThrow(new SolicitudNotFoundException(99L));

        mockMvc.perform(get("/api/farmacia/solicitudes/99"))
                .andExpect(status().isNotFound());
    }
}
