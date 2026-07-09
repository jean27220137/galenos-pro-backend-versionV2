package com.galenospro.almacen.controller;

import com.galenospro.almacen.config.SecurityConfig;
import com.galenospro.almacen.entity.Almacen;
import com.galenospro.almacen.exception.GlobalExceptionHandler;
import com.galenospro.almacen.repository.AlmacenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlmacenController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AlmacenControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AlmacenRepository almacenRepository;

    private Almacen buildAlmacen() {
        Almacen a = new Almacen();
        a.setId(1L);
        a.setCodigo("ALM-001");
        a.setNombre("Almacen Central");
        a.setActivo(1);
        return a;
    }

    @Test
    @WithMockUser
    void GET_listar_retorna_200_con_almacenes() throws Exception {
        when(almacenRepository.findAllByActivo(1)).thenReturn(List.of(buildAlmacen()));

        mockMvc.perform(get("/api/almacen/almacenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("ALM-001"))
                .andExpect(jsonPath("$[0].nombre").value("Almacen Central"));
    }

    @Test
    @WithMockUser
    void GET_listar_retorna_200_lista_vacia() throws Exception {
        when(almacenRepository.findAllByActivo(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/almacen/almacenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_existente_retorna_200() throws Exception {
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(buildAlmacen()));

        mockMvc.perform(get("/api/almacen/almacenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("ALM-001"));
    }

    @Test
    @WithMockUser
    void GET_buscarPorId_inexistente_retorna_404() throws Exception {
        when(almacenRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/almacen/almacenes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
