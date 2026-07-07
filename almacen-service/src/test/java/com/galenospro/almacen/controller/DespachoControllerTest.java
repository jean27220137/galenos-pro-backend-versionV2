package com.galenospro.almacen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.almacen.config.SecurityConfig;
import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.exception.GlobalExceptionHandler;
import com.galenospro.almacen.messaging.AlmacenPublisher;
import com.galenospro.almacen.service.NotaSalidaService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DespachoController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class DespachoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean NotaSalidaService notaSalidaService;
    @MockitoBean AlmacenPublisher almacenPublisher;

    private DespachoSolicitudDto buildDto() {
        DespachoSolicitudDto dto = new DespachoSolicitudDto();
        dto.setSolicitudId(1L);
        dto.setAlmacenId(1L);
        dto.setAlmacenDestinoId(2L);
        dto.setFarmaciaId(3L);
        dto.setDetalles(List.of());
        return dto;
    }

    private NotaSalidaResponseDto buildNota() {
        return NotaSalidaResponseDto.builder()
                .id(1L).nroNotaSalida("NS-000001")
                .solicitudId(1L).estado("GENERADA").build();
    }

    @Test
    @WithMockUser(authorities = "ALMACENERO")
    void POST_despachar_retorna_201_con_nota() throws Exception {
        when(notaSalidaService.despacharSolicitud(any())).thenReturn(buildNota());
        doNothing().when(almacenPublisher).publicarDespachoConfirmado(any(), any());

        mockMvc.perform(post("/api/almacen/despacho")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nroNotaSalida").value("NS-000001"));

        verify(almacenPublisher).publicarDespachoConfirmado(any(), any());
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void POST_despachar_sin_rol_almacenero_retorna_403() throws Exception {
        mockMvc.perform(post("/api/almacen/despacho")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDto())))
                .andExpect(status().isForbidden());
    }
}
