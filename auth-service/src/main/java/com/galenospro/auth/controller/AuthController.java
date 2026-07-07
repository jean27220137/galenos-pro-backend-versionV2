package com.galenospro.auth.controller;

import com.galenospro.auth.dto.LoginRequestDto;
import com.galenospro.auth.dto.LoginResponseDto;
import com.galenospro.auth.dto.ValidateTokenResponseDto;
import com.galenospro.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, logout y validación de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso, retorna JWT"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario inactivo"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Cerrar sesión — invalida el token en Redis")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logout exitoso"),
        @ApiResponse(responseCode = "401", description = "Token no proporcionado")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Validar token JWT — usado por el gateway")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token válido, retorna claims"),
        @ApiResponse(responseCode = "401", description = "Token inválido, expirado o en blacklist")
    })
    @GetMapping("/validate")
    public ResponseEntity<ValidateTokenResponseDto> validate(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
