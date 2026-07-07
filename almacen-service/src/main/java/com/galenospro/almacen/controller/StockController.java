package com.galenospro.almacen.controller;

import com.galenospro.almacen.dto.EntradaStockRequestDto;
import com.galenospro.almacen.dto.StockResponseDto;
import com.galenospro.almacen.service.StockService;
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

@Tag(name = "Stock", description = "Gestión de stock del almacén")
@RestController
@RequestMapping("/api/almacen/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Listar todo el stock de un almacén")
    @ApiResponse(responseCode = "200", description = "Stock listado correctamente")
    @GetMapping
    public ResponseEntity<List<StockResponseDto>> listarPorAlmacen(
            @RequestParam Long almacenId) {
        return ResponseEntity.ok(stockService.listarPorAlmacen(almacenId));
    }

    @Operation(summary = "Listar lotes de un medicamento en un almacén")
    @ApiResponse(responseCode = "200", description = "Lotes encontrados")
    @GetMapping("/{medicamentoId}")
    public ResponseEntity<List<StockResponseDto>> listarPorMedicamento(
            @PathVariable Long medicamentoId,
            @RequestParam Long almacenId) {
        return ResponseEntity.ok(stockService.listarPorMedicamento(medicamentoId, almacenId));
    }

    @Operation(summary = "Consultar cantidad disponible de un medicamento")
    @ApiResponse(responseCode = "200", description = "Cantidad disponible")
    @GetMapping("/{medicamentoId}/disponible")
    public ResponseEntity<Integer> consultarDisponible(
            @PathVariable Long medicamentoId,
            @RequestParam Long almacenId) {
        return ResponseEntity.ok(stockService.consultarDisponible(medicamentoId, almacenId));
    }

    @Operation(summary = "Registrar entrada de stock (lote)")
    @ApiResponse(responseCode = "201", description = "Entrada registrada")
    @PostMapping("/entrada")
    @PreAuthorize("hasAnyAuthority('ADMIN','ALMACENERO')")
    public ResponseEntity<StockResponseDto> registrarEntrada(@Valid @RequestBody EntradaStockRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.registrarEntrada(dto));
    }
}
