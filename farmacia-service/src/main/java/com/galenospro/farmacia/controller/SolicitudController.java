package com.galenospro.farmacia.controller;

import com.galenospro.farmacia.dto.SolicitudRequestDto;
import com.galenospro.farmacia.dto.SolicitudResponseDto;
import com.galenospro.farmacia.service.SolicitudService;
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
import java.util.Map;
import java.util.HashMap;

@Tag(name = "Solicitudes", description = "Gestión de solicitudes de requerimiento de medicamentos")
@RestController
@RequestMapping("/api/farmacia/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @Operation(summary = "Crear solicitud de requerimiento")
    @ApiResponse(responseCode = "201", description = "Solicitud creada y publicada en RabbitMQ")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('JEFE_FARMACIA','FARMACEUTICO')")
    public ResponseEntity<SolicitudResponseDto> crear(
            @Valid @RequestBody SolicitudRequestDto dto,
            @RequestHeader("X-User-Id") Long farmaceuticoId,
            @RequestHeader(value = "X-Farmacia-Id", required = false) Long farmaciaIdHeader) {
        if (dto.getFarmaciaId() == null && farmaciaIdHeader != null) {
            dto.setFarmaciaId(farmaciaIdHeader);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.crear(dto, farmaceuticoId));
    }

    @Operation(summary = "Listar solicitudes activas (PENDIENTE + APROBADO_JEFE + EN_PROCESO)")
    @ApiResponse(responseCode = "200", description = "Solicitudes activas")
    @GetMapping("/activas")
    public ResponseEntity<List<SolicitudResponseDto>> listarActivas() {
        return ResponseEntity.ok(solicitudService.listarActivas());
    }

    @Operation(summary = "Listar solicitudes (por farmacia o por estado)")
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes")
    @GetMapping
    public ResponseEntity<List<SolicitudResponseDto>> listar(
            @RequestParam(required = false) Long farmaciaId,
            @RequestParam(required = false) String estado) {
        if (farmaciaId != null) {
            return ResponseEntity.ok(solicitudService.listarPorFarmacia(farmaciaId));
        }
        if (estado != null) {
            return ResponseEntity.ok(solicitudService.listarPorEstado(estado));
        }
        return ResponseEntity.ok(List.of());
    }

    @Operation(summary = "Buscar solicitud por ID (con detalles)")
    @ApiResponse(responseCode = "200", description = "Solicitud encontrada")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.buscarPorId(id));
    }

    @Operation(summary = "Consultar estado de una solicitud")
    @ApiResponse(responseCode = "200", description = "Estado retornado")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @GetMapping("/{id}/estado")
    public ResponseEntity<Map<String, String>> consultarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("estado", solicitudService.consultarEstado(id)));
    }

    @Operation(summary = "Jefe de farmacia aprueba la solicitud → APROBADO_JEFE")
    @ApiResponse(responseCode = "204", description = "Solicitud aprobada")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @ApiResponse(responseCode = "409", description = "La solicitud no está en estado PENDIENTE")
    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyAuthority('JEFE_FARMACIA','ADMIN')")
    public ResponseEntity<Void> aprobar(@PathVariable Long id) {
        solicitudService.aprobar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Almacenero toma la solicitud → EN_PROCESO")
    @ApiResponse(responseCode = "204", description = "Solicitud marcada EN_PROCESO")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @ApiResponse(responseCode = "409", description = "La solicitud no está en estado PENDIENTE")
    @PutMapping("/{id}/en-proceso")
    @PreAuthorize("hasAnyAuthority('ALMACENERO','ADMIN')")
    public ResponseEntity<Void> marcarEnProceso(@PathVariable Long id) {
        solicitudService.marcarEnProceso(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Farmacia confirma recepción de medicamentos → ENTREGADA")
    @ApiResponse(responseCode = "204", description = "Entrega confirmada")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @ApiResponse(responseCode = "409", description = "La solicitud no está en estado DESPACHADA")
    @PutMapping("/{id}/entregar")
    @PreAuthorize("hasAnyAuthority('JEFE_FARMACIA','FARMACEUTICO','ALMACENERO','ADMIN')")
    public ResponseEntity<Void> confirmarEntrega(@PathVariable Long id) {
        solicitudService.confirmarEntrega(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Farmacia rechaza la entrega → RECHAZADA")
    @ApiResponse(responseCode = "204", description = "Solicitud rechazada")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @ApiResponse(responseCode = "409", description = "La solicitud no está en estado DESPACHADA")
    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyAuthority('JEFE_FARMACIA','FARMACEUTICO','ADMIN')")
    public ResponseEntity<Void> rechazar(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        solicitudService.rechazar(id, body.get("motivo"));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancelar solicitud (solo PENDIENTE)")
    @ApiResponse(responseCode = "204", description = "Solicitud cancelada")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @ApiResponse(responseCode = "409", description = "La solicitud no puede cancelarse en su estado actual")
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('JEFE_FARMACIA','FARMACEUTICO')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        solicitudService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
