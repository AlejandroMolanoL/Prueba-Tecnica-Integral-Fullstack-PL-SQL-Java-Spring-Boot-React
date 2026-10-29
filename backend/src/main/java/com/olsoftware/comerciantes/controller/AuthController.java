package com.olsoftware.comerciantes.controller;

import com.olsoftware.comerciantes.dto.ApiResponse;
import com.olsoftware.comerciantes.dto.AuthRequest;
import com.olsoftware.comerciantes.dto.AuthResponse;
import com.olsoftware.comerciantes.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Seguridad y Autenticación", description = "Endpoints de login y generación de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT", description = "Endpoint público para autenticación de Administradores y Auxiliares de Registro")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.autenticar(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Autenticación exitosa"));
    }
}
