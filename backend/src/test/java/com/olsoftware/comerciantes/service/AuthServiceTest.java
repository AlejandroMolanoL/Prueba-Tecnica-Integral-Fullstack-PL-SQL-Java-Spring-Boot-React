package com.olsoftware.comerciantes.service;

import com.olsoftware.comerciantes.config.JwtTokenProvider;
import com.olsoftware.comerciantes.dto.AuthRequest;
import com.olsoftware.comerciantes.dto.AuthResponse;
import com.olsoftware.comerciantes.exception.BusinessException;
import com.olsoftware.comerciantes.model.Rol;
import com.olsoftware.comerciantes.model.Usuario;
import com.olsoftware.comerciantes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Admin Test")
                .correoElectronico("admin@olsoftware.com")
                .contrasena("encodedPass123")
                .rol(Rol.ADMINISTRADOR)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe autenticar correctamente con credenciales válidas y retornar JWT")
    void testAutenticarExitoso() {
        AuthRequest request = AuthRequest.builder()
                .correoElectronico("admin@olsoftware.com")
                .contrasena("Admin123*")
                .build();

        when(usuarioRepository.findByCorreoElectronico("admin@olsoftware.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("Admin123*", "encodedPass123")).thenReturn(true);
        when(tokenProvider.generarToken("admin@olsoftware.com", "Admin Test", Rol.ADMINISTRADOR)).thenReturn("jwt.token.mock");
        when(tokenProvider.getJwtExpirationMs()).thenReturn(3600000L);

        AuthResponse response = authService.autenticar(request);

        assertNotNull(response);
        assertEquals("jwt.token.mock", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("ADMINISTRADOR", response.getRol());
        assertEquals("admin@olsoftware.com", response.getCorreoElectronico());
        verify(usuarioRepository, times(1)).findByCorreoElectronico("admin@olsoftware.com");
    }

    @Test
    @DisplayName("Debe fallar si las contraseñas no coinciden")
    void testAutenticarPasswordIncorrecta() {
        AuthRequest request = AuthRequest.builder()
                .correoElectronico("admin@olsoftware.com")
                .contrasena("WrongPassword")
                .build();

        when(usuarioRepository.findByCorreoElectronico("admin@olsoftware.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("WrongPassword", "encodedPass123")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.autenticar(request));
    }

    @Test
    @DisplayName("Debe fallar si el usuario no existe")
    void testAutenticarUsuarioNoExiste() {
        AuthRequest request = AuthRequest.builder()
                .correoElectronico("noexiste@olsoftware.com")
                .contrasena("Pass123*")
                .build();

        when(usuarioRepository.findByCorreoElectronico("noexiste@olsoftware.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.autenticar(request));
    }
}
