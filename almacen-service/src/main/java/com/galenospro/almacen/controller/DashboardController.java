package com.galenospro.almacen.controller;

import com.galenospro.almacen.dto.ProximoVencerDTO;
import com.galenospro.almacen.dto.StockCriticoDTO;
import com.galenospro.almacen.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "Resumen de stock crítico y vencimientos para el almacén")
@RestController
@RequestMapping("/api/almacen/dashboard")
@PreAuthorize("hasAnyAuthority('ALMACENERO','ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Medicamentos con stock total igual o por debajo del mínimo")
    @ApiResponse(responseCode = "200", description = "Lista de hasta 10 ítems más críticos")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @GetMapping("/stock-critico")
    public ResponseEntity<List<StockCriticoDTO>> getStockCritico() {
        return ResponseEntity.ok(dashboardService.getStockCritico());
    }

    @Operation(summary = "Lotes de medicamentos próximos a vencer")
    @ApiResponse(responseCode = "200", description = "Lista de hasta 10 lotes más próximos")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @GetMapping("/proximos-vencer")
    public ResponseEntity<List<ProximoVencerDTO>> getProximosAVencer(
            @RequestParam(defaultValue = "90") int dias) {
        return ResponseEntity.ok(dashboardService.getProximosAVencer(dias));
    }
}
