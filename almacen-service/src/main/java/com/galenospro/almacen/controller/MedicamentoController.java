package com.galenospro.almacen.controller;

import com.galenospro.almacen.dto.MedicamentoRequestDto;
import com.galenospro.almacen.dto.MedicamentoResponseDto;
import com.galenospro.almacen.service.MedicamentoService;
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

@Tag(name = "Medicamentos", description = "Catálogo de medicamentos del almacén")
@RestController
@RequestMapping("/api/almacen/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @Operation(summary = "Listar catálogo de medicamentos activos")
    @ApiResponse(responseCode = "200", description = "Lista de medicamentos")
    @GetMapping
    public ResponseEntity<List<MedicamentoResponseDto>> listar() {
        return ResponseEntity.ok(medicamentoService.listar());
    }

    @Operation(summary = "Buscar medicamento por ID")
    @ApiResponse(responseCode = "200", description = "Medicamento encontrado")
    @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.buscarPorId(id));
    }

    @Operation(summary = "Registrar nuevo medicamento")
    @ApiResponse(responseCode = "201", description = "Medicamento creado")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','ALMACENERO')")
    public ResponseEntity<MedicamentoResponseDto> crear(@Valid @RequestBody MedicamentoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoService.crear(dto));
    }
}
