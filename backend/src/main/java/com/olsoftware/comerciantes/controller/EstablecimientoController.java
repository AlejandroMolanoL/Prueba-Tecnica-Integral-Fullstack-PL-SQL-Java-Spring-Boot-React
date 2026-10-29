package com.olsoftware.comerciantes.controller;

import com.olsoftware.comerciantes.dto.ApiResponse;
import com.olsoftware.comerciantes.dto.EstablecimientoDTO;
import com.olsoftware.comerciantes.service.EstablecimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/establecimientos")
@RequiredArgsConstructor
@Tag(name = "Establecimientos", description = "Endpoints para la gestión CRUD de establecimientos de comercio")
public class EstablecimientoController {

    private final EstablecimientoService establecimientoService;

    @GetMapping("/comerciante/{comercianteId}")
    @Operation(summary = "Consultar establecimientos asociados a un comerciante", description = "Retorna todos los establecimientos registrados a nombre del comerciante")
    public ResponseEntity<ApiResponse<List<EstablecimientoDTO>>> listarPorComerciante(@PathVariable Long comercianteId) {
        List<EstablecimientoDTO> lista = establecimientoService.obtenerPorComerciante(comercianteId);
        return ResponseEntity.ok(ApiResponse.ok(lista, "Establecimientos consultados con éxito"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar establecimiento por ID", description = "Retorna el detalle de un establecimiento específico")
    public ResponseEntity<ApiResponse<EstablecimientoDTO>> obtenerPorId(@PathVariable Long id) {
        EstablecimientoDTO dto = establecimientoService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok(dto, "Establecimiento encontrado"));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo establecimiento", description = "Registra un establecimiento con nombre, ingresos y número de empleados")
    public ResponseEntity<ApiResponse<EstablecimientoDTO>> crear(@Valid @RequestBody EstablecimientoDTO dto) {
        EstablecimientoDTO creado = establecimientoService.crear(dto);
        return ResponseEntity.ok(ApiResponse.ok(creado, "Establecimiento creado con éxito"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar establecimiento", description = "Actualiza los datos del establecimiento existente")
    public ResponseEntity<ApiResponse<EstablecimientoDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstablecimientoDTO dto
    ) {
        EstablecimientoDTO actualizado = establecimientoService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(actualizado, "Establecimiento actualizado con éxito"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar establecimiento", description = "Elimina un establecimiento por su ID")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        establecimientoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Establecimiento eliminado con éxito"));
    }
}
