package com.galenospro.almacen.controller;

import com.galenospro.almacen.config.SecurityConfig;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.exception.GlobalExceptionHandler;
import com.galenospro.almacen.exception.NotaSalidaNotFoundException;
import com.galenospro.almacen.service.NotaSalidaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotaSalidaController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class NotaSalidaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean NotaSalidaService notaSalidaService;

    private NotaSalidaResponseDto buildNota() {
        return NotaSalidaResponseDto.builder()
                .id(1L).nroNotaSalida("NS-000001")
                .solicitudId(1L).estado("GENERADA").build();
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void GET_listarPorAlmacen_retorna_200() throws Exception {
        when(notaSalidaService.listarPorAlmacen(1L)).thenReturn(List.of(buildNota()));

        mockMvc.perform(get("/api/almacen/notas-salida").param("almacenId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nroNotaSalida").value("NS-000001"));
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void GET_buscarPorId_existente_retorna_200() throws Exception {
        when(notaSalidaService.buscarPorId(1L)).thenReturn(buildNota());

        mockMvc.perform(get("/api/almacen/notas-salida/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nroNotaSalida").value("NS-000001"))
                .andExpect(jsonPath("$.estado").value("GENERADA"));
    }

    @Test
    @WithMockUser(authorities = "JEFE_FARMACIA")
    void GET_buscarPorId_inexistente_retorna_404() throws Exception {
        when(notaSalidaService.buscarPorId(99L)).thenThrow(new NotaSalidaNotFoundException(99L));

        mockMvc.perform(get("/api/almacen/notas-salida/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void PUT_confirmarEntrega_retorna_204() throws Exception {
        doNothing().when(notaSalidaService).confirmarEntrega(1L);

        mockMvc.perform(put("/api/almacen/notas-salida/1/entregar"))
                .andExpect(status().isNoContent());

        verify(notaSalidaService).confirmarEntrega(1L);
    }

    @Test
    void PUT_confirmarEntrega_sin_auth_retorna_403() throws Exception {
        mockMvc.perform(put("/api/almacen/notas-salida/1/entregar"))
                .andExpect(status().isForbidden());
    }
}
