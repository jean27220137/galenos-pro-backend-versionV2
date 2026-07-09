package com.galenospro.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.auth.dto.RegistrarUsuarioRequestDto;
import com.galenospro.auth.dto.UsuarioResponseDto;
import com.galenospro.auth.config.SecurityConfig;
import com.galenospro.auth.exception.EmailDuplicadoException;
import com.galenospro.auth.exception.GlobalExceptionHandler;
import com.galenospro.auth.exception.UsuarioNotFoundException;
import com.galenospro.auth.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UsuarioControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private UsuarioService usuarioService;

    private RegistrarUsuarioRequestDto buildRequest() {
        RegistrarUsuarioRequestDto dto = new RegistrarUsuarioRequestDto();
        dto.setNombres("Ana"); dto.setApellidos("Torres");
        dto.setEmail("ana@bernales.gob.pe"); dto.setPassword("clave123");
        dto.setRol("JEFE_FARMACIA"); dto.setFarmaciaId(1L);
        return dto;
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void POST_register_con_rol_admin_retorna_201() throws Exception {
        UsuarioResponseDto response = UsuarioResponseDto.builder()
                .id(1L).nombres("Ana").apellidos("Torres")
                .email("ana@bernales.gob.pe").rol("JEFE_FARMACIA")
                .farmaciaId(1L).activo(1)
                .build();

        when(usuarioService.registrar(any(RegistrarUsuarioRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rol").value("JEFE_FARMACIA"));
    }

    @Test
    @WithMockUser(authorities = "FARMACEUTICO")
    void POST_register_sin_rol_admin_retorna_403() throws Exception {
        mockMvc.perform(post("/api/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void GET_usuarios_retorna_lista() throws Exception {
        UsuarioResponseDto u = UsuarioResponseDto.builder()
                .id(1L).nombres("Ana").rol("JEFE_FARMACIA").activo(1).build();
        when(usuarioService.listar()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/auth/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void DELETE_usuario_retorna_204() throws Exception {
        doNothing().when(usuarioService).desactivar(eq(1L));

        mockMvc.perform(delete("/api/auth/usuarios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_actualizar_usuario_retorna_200() throws Exception {
        UsuarioResponseDto response = UsuarioResponseDto.builder()
                .id(1L).nombres("Ana").apellidos("Torres")
                .email("ana@bernales.gob.pe").rol("ADMIN")
                .farmaciaId(1L).activo(1)
                .build();

        when(usuarioService.actualizar(eq(1L), any(RegistrarUsuarioRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/auth/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void POST_register_409_email_duplicado() throws Exception {
        when(usuarioService.registrar(any(RegistrarUsuarioRequestDto.class)))
                .thenThrow(new EmailDuplicadoException("ana@bernales.gob.pe"));

        mockMvc.perform(post("/api/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void POST_register_400_body_invalido() throws Exception {
        mockMvc.perform(post("/api/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void PUT_actualizar_404_usuario_no_encontrado() throws Exception {
        when(usuarioService.actualizar(eq(99L), any(RegistrarUsuarioRequestDto.class)))
                .thenThrow(new UsuarioNotFoundException(99L));

        mockMvc.perform(put("/api/auth/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void DELETE_desactivar_404_usuario_no_encontrado() throws Exception {
        org.mockito.Mockito.doThrow(new UsuarioNotFoundException(99L))
                .when(usuarioService).desactivar(99L);

        mockMvc.perform(delete("/api/auth/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void GET_usuarios_500_error_generico() throws Exception {
        when(usuarioService.listar()).thenThrow(new RuntimeException("DB connection lost"));

        mockMvc.perform(get("/api/auth/usuarios"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));
    }
}
