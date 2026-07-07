package com.galenospro.almacen.controller;

import com.galenospro.almacen.dto.DespachoSolicitudDto;
import com.galenospro.almacen.dto.NotaSalidaResponseDto;
import com.galenospro.almacen.messaging.AlmacenPublisher;
import com.galenospro.almacen.service.NotaSalidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Despacho", description = "Despacho manual de solicitudes por el almacenero")
@RestController
@RequestMapping("/api/almacen/despacho")
@RequiredArgsConstructor
public class DespachoController {

    private final NotaSalidaService notaSalidaService;
    private final AlmacenPublisher almacenPublisher;

    @Operation(summary = "Despachar solicitud manualmente y generar Nota de Salida")
    @ApiResponse(responseCode = "201", description = "Nota de Salida generada")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ALMACENERO','ADMIN')")
    public ResponseEntity<NotaSalidaResponseDto> despachar(
            @RequestBody DespachoSolicitudDto dto,
            @RequestHeader("X-User-Id") Long despachadorId) {
        dto.setDespachadorId(despachadorId);
        NotaSalidaResponseDto nota = notaSalidaService.despacharSolicitud(dto);
        almacenPublisher.publicarDespachoConfirmado(nota, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nota);
    }
}
