package com.galenospro.almacen.controller;

import com.galenospro.almacen.entity.Almacen;
import com.galenospro.almacen.exception.AlmacenNotFoundException;
import com.galenospro.almacen.repository.AlmacenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Almacenes", description = "Catálogo de almacenes")
@RestController
@RequestMapping("/api/almacen/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenRepository almacenRepository;

    @Operation(summary = "Listar almacenes activos")
    @ApiResponse(responseCode = "200", description = "Lista de almacenes")
    @GetMapping
    public ResponseEntity<List<Almacen>> listar() {
        return ResponseEntity.ok(almacenRepository.findAllByActivo(1));
    }

    @Operation(summary = "Buscar almacén por ID")
    @ApiResponse(responseCode = "200", description = "Almacén encontrado")
    @ApiResponse(responseCode = "404", description = "Almacén no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<Almacen> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                almacenRepository.findById(id)
                        .orElseThrow(() -> new AlmacenNotFoundException(id))
        );
    }
}
