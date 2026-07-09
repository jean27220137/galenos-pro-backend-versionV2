package com.galenospro.farmacia.controller;

import com.galenospro.farmacia.dto.FarmaciaRequestDto;
import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.service.FarmaciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Farmacias", description = "Gestión de farmacias del Hospital Bernales")
@RestController
@RequestMapping("/api/farmacia/farmacias")
@RequiredArgsConstructor
public class FarmaciaController {

    private final FarmaciaService farmaciaService;

    @Operation(summary = "Listar farmacias activas")
    @ApiResponse(responseCode = "200", description = "Lista de farmacias activas")
    @GetMapping
    public ResponseEntity<List<FarmaciaResponseDto>> listar() {
        return ResponseEntity.ok(farmaciaService.listar());
    }

    @Operation(summary = "Listar todas las farmacias (activas e inactivas) — solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Lista completa de farmacias")
    @GetMapping("/todas")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<FarmaciaResponseDto>> listarTodas() {
        return ResponseEntity.ok(farmaciaService.listarTodas());
    }

    @Operation(summary = "Buscar farmacia por ID")
    @ApiResponse(responseCode = "200", description = "Farmacia encontrada")
    @ApiResponse(responseCode = "404", description = "Farmacia no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<FarmaciaResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(farmaciaService.buscarPorId(id));
    }

    @Operation(summary = "Crear nueva farmacia — solo ADMIN")
    @ApiResponse(responseCode = "201", description = "Farmacia creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o código duplicado")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FarmaciaResponseDto> crear(@Valid @RequestBody FarmaciaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmaciaService.crear(dto));
    }

    @Operation(summary = "Actualizar farmacia — solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Farmacia actualizada")
    @ApiResponse(responseCode = "404", description = "Farmacia no encontrada")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FarmaciaResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FarmaciaRequestDto dto) {
        return ResponseEntity.ok(farmaciaService.actualizar(id, dto));
    }

    @Operation(summary = "Desactivar farmacia — solo ADMIN")
    @ApiResponse(responseCode = "204", description = "Farmacia desactivada")
    @ApiResponse(responseCode = "404", description = "Farmacia no encontrada")
    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        farmaciaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar farmacia — solo ADMIN")
    @ApiResponse(responseCode = "204", description = "Farmacia activada")
    @ApiResponse(responseCode = "404", description = "Farmacia no encontrada")
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        farmaciaService.activar(id);
        return ResponseEntity.noContent().build();
    }
}
