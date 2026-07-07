package com.galenospro.almacen.controller;

import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.service.NotaSalidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notas de Salida", description = "Despacho y entrega de medicamentos")
@RestController
@RequestMapping("/api/almacen/notas-salida")
@RequiredArgsConstructor
public class NotaSalidaController {

    private final NotaSalidaService notaSalidaService;

    @Operation(summary = "Listar notas de salida de un almacén")
    @ApiResponse(responseCode = "200", description = "Notas listadas")
    @GetMapping
    public ResponseEntity<List<NotaSalidaResponseDto>> listarPorAlmacen(
            @RequestParam Long almacenId) {
        return ResponseEntity.ok(notaSalidaService.listarPorAlmacen(almacenId));
    }

    @Operation(summary = "Buscar nota de salida por ID")
    @ApiResponse(responseCode = "200", description = "Nota encontrada")
    @ApiResponse(responseCode = "404", description = "Nota no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<NotaSalidaResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(notaSalidaService.buscarPorId(id));
    }

    @Operation(summary = "Confirmar entrega de nota de salida")
    @ApiResponse(responseCode = "204", description = "Entrega confirmada")
    @ApiResponse(responseCode = "404", description = "Nota no encontrada")
    @PutMapping("/{id}/entregar")
    @PreAuthorize("hasAnyAuthority('ADMIN','ALMACENERO')")
    public ResponseEntity<Void> confirmarEntrega(@PathVariable Long id) {
        notaSalidaService.confirmarEntrega(id);
        return ResponseEntity.noContent().build();
    }
}
