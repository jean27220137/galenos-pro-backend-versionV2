package com.galenospro.auth.controller;

import com.galenospro.auth.dto.RegistrarUsuarioRequestDto;
import com.galenospro.auth.dto.UsuarioResponseDto;
import com.galenospro.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del hospital (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Registrar nuevo usuario del hospital")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "409", description = "Email ya registrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere rol ADMIN")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UsuarioResponseDto> registrar(
            @Valid @RequestBody RegistrarUsuarioRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.registrar(dto));
    }

    @Operation(summary = "Listar todos los usuarios activos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere rol ADMIN")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDto>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @Operation(summary = "Actualizar datos de un usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere rol ADMIN")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UsuarioResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarUsuarioRequestDto dto) {
        return ResponseEntity.ok(usuarioService.actualizar(id, dto));
    }

    @Operation(summary = "Desactivar un usuario (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario desactivado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere rol ADMIN")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
