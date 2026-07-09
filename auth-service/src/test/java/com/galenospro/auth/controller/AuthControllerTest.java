package com.galenospro.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galenospro.auth.dto.LoginRequestDto;
import com.galenospro.auth.dto.LoginResponseDto;
import com.galenospro.auth.dto.ValidateTokenResponseDto;
import com.galenospro.auth.exception.CredencialesInvalidasException;
import com.galenospro.auth.exception.UsuarioInactivoException;
import com.galenospro.auth.config.SecurityConfig;
import com.galenospro.auth.exception.GlobalExceptionHandler;
import com.galenospro.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;

    @Test
    void POST_login_retorna_200_con_jwt() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("juan@bernales.gob.pe");
        request.setPassword("clave123");

        LoginResponseDto response = LoginResponseDto.builder()
                .token("jwt-generado").rol("FARMACEUTICO")
                .userId(1L).farmaciaId(2L)
                .expira(LocalDateTime.now().plusMinutes(30))
                .build();

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-generado"))
                .andExpect(jsonPath("$.rol").value("FARMACEUTICO"));
    }

    @Test
    void POST_login_retorna_401_credenciales_invalidas() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("mal@test.com");
        request.setPassword("malo");

        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    @WithMockUser
    void POST_logout_retorna_200() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk());
    }

    @Test
    void GET_validate_retorna_200_con_claims() throws Exception {
        ValidateTokenResponseDto response = ValidateTokenResponseDto.builder()
                .userId(1L).email("juan@bernales.gob.pe")
                .rol("FARMACEUTICO").farmaciaId(2L)
                .build();

        when(authService.validateToken("token-ok")).thenReturn(response);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer token-ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("FARMACEUTICO"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void POST_login_400_body_invalido() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void POST_login_401_usuario_inactivo() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("inactivo@bernales.gob.pe");
        request.setPassword("clave123");

        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new UsuarioInactivoException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario inactivo"));
    }
}
