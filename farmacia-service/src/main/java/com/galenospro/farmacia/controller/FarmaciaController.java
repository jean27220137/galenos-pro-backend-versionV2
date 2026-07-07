package com.galenospro.farmacia.controller;

import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.service.FarmaciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Farmacias", description = "Catálogo de farmacias del hospital")
@RestController
@RequestMapping("/api/farmacia/farmacias")
@RequiredArgsConstructor
public class FarmaciaController {

    private final FarmaciaService farmaciaService;

    @Operation(summary = "Listar farmacias activas")
    @ApiResponse(responseCode = "200", description = "Lista de farmacias")
    @GetMapping
    public ResponseEntity<List<FarmaciaResponseDto>> listar() {
        return ResponseEntity.ok(farmaciaService.listar());
    }

    @Operation(summary = "Buscar farmacia por ID")
    @ApiResponse(responseCode = "200", description = "Farmacia encontrada")
    @ApiResponse(responseCode = "404", description = "Farmacia no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<FarmaciaResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(farmaciaService.buscarPorId(id));
    }
}
