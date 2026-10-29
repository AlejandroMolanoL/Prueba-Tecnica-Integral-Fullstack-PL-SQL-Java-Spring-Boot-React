package com.olsoftware.comerciantes.controller;

import com.olsoftware.comerciantes.dto.*;
import com.olsoftware.comerciantes.service.ComercianteService;
import com.olsoftware.comerciantes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/comerciantes")
@RequiredArgsConstructor
@Tag(name = "Comerciantes", description = "Endpoints para la gestión CRUD, estados y reportes de comerciantes")
public class ComercianteController {

    private final ComercianteService comercianteService;
    private final ReporteService reporteService;

    @GetMapping
    @Operation(summary = "Consulta paginada de comerciantes", description = "Permite filtrar por nombre, municipio, fecha de registro y estado. Paginación por defecto de 5 registros")
    public ResponseEntity<ApiResponse<PaginatedResponse<ComercianteDTO>>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRegistro,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "5") int registrosPorPagina
    ) {
        PaginatedResponse<ComercianteDTO> resultado = comercianteService.listarComerciantesPaginados(
                nombre, municipio, fechaRegistro, estado, pagina, registrosPorPagina
        );
        return ResponseEntity.ok(ApiResponse.ok(resultado, "Comerciantes consultados con éxito"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar comerciante por ID", description = "Retorna el comerciante con sus totales calculados de activos y empleados")
    public ResponseEntity<ApiResponse<ComercianteDTO>> obtenerPorId(@PathVariable Long id) {
        ComercianteDTO dto = comercianteService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.ok(dto, "Comerciante encontrado"));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo comerciante", description = "Registra un nuevo comerciante con validación de obligatoriedad, formatos y establecimientos opcionales")
    public ResponseEntity<ApiResponse<ComercianteDTO>> crear(@Valid @RequestBody ComercianteCreateDTO dto) {
        ComercianteDTO creado = comercianteService.crear(dto);
        return ResponseEntity.ok(ApiResponse.ok(creado, "Comerciante creado exitosamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comerciante", description = "Actualiza los datos del comerciante existente")
    public ResponseEntity<ApiResponse<ComercianteDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComercianteUpdateDTO dto
    ) {
        ComercianteDTO actualizado = comercianteService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(actualizado, "Comerciante actualizado exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Modificar estado del comerciante (Activar/Inactivar)", description = "Cambia el estado del comerciante vía PATCH (Reto 08)")
    public ResponseEntity<ApiResponse<ComercianteDTO>> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoComercianteDTO dto
    ) {
        ComercianteDTO actualizado = comercianteService.cambiarEstado(id, dto.getNuevoEstado());
        return ResponseEntity.ok(ApiResponse.ok(actualizado, "Estado del comerciante actualizado a: " + dto.getNuevoEstado()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar comerciante", description = "Elimina un comerciante y sus establecimientos. Solo accesible para el rol Administrador")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        comercianteService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Comerciante eliminado correctamente"));
    }

    @GetMapping("/reporte/csv")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Descargar reporte en archivo plano CSV", description = "Genera archivo CSV delimitado por pipe (|) con comerciantes registrados y activos. Solo rol Administrador")
    public ResponseEntity<byte[]> descargarReporteCsv() {
        byte[] csvData = reporteService.generarReporteComerciantesCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_comerciantes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/{id}/ficha/pdf")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Descargar Ficha del Comerciante en PDF", description = "Genera un documento PDF detallado con la información y establecimientos del comerciante. Solo rol Administrador")
    public ResponseEntity<byte[]> descargarFichaPdf(@PathVariable Long id) {
        byte[] pdfData = reporteService.generarFichaComerciantePdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ficha_comerciante_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}
