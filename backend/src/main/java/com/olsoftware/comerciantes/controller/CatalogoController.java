package com.olsoftware.comerciantes.controller;

import com.olsoftware.comerciantes.dto.ApiResponse;
import com.olsoftware.comerciantes.dto.DepartamentoDTO;
import com.olsoftware.comerciantes.dto.MunicipioDTO;
import com.olsoftware.comerciantes.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Listas de Valores", description = "Endpoints cacheados en memoria para departamentos y municipios")
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/departamentos")
    @Operation(summary = "Listar departamentos", description = "Retorna lista de departamentos almacenados en caché")
    public ResponseEntity<ApiResponse<List<DepartamentoDTO>>> listarDepartamentos() {
        List<DepartamentoDTO> lista = catalogoService.obtenerDepartamentos();
        return ResponseEntity.ok(ApiResponse.ok(lista, "Departamentos recuperados con éxito"));
    }

    @GetMapping("/departamentos/{id}/municipios")
    @Operation(summary = "Listar municipios por ID de departamento", description = "Retorna lista de municipios asociados a un departamento")
    public ResponseEntity<ApiResponse<List<MunicipioDTO>>> listarMunicipiosPorDepartamento(@PathVariable Long id) {
        List<MunicipioDTO> lista = catalogoService.obtenerMunicipiosPorDepartamento(id);
        return ResponseEntity.ok(ApiResponse.ok(lista, "Municipios recuperados con éxito"));
    }

    @GetMapping("/municipios")
    @Operation(summary = "Listar municipios por nombre de departamento", description = "Permite filtrar municipios pasando el nombre del departamento")
    public ResponseEntity<ApiResponse<List<MunicipioDTO>>> listarMunicipiosPorNombre(
            @RequestParam(required = false) String departamento) {
        List<MunicipioDTO> lista;
        if (departamento != null && !departamento.isBlank()) {
            lista = catalogoService.obtenerMunicipiosPorNombreDepartamento(departamento);
        } else {
            lista = List.of();
        }
        return ResponseEntity.ok(ApiResponse.ok(lista, "Municipios recuperados con éxito"));
    }
}
