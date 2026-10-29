package com.olsoftware.comerciantes.service;

import com.olsoftware.comerciantes.config.JwtTokenProvider;
import com.olsoftware.comerciantes.dto.AuthRequest;
import com.olsoftware.comerciantes.dto.AuthResponse;
import com.olsoftware.comerciantes.exception.BusinessException;
import com.olsoftware.comerciantes.model.Usuario;
import com.olsoftware.comerciantes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse autenticar(AuthRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(request.getCorreoElectronico().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessException("El usuario se encuentra inactivo en la plataforma");
        }

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }

        String token = tokenProvider.generarToken(usuario.getCorreoElectronico(), usuario.getNombre(), usuario.getRol());

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEn(tokenProvider.getJwtExpirationMs())
                .nombre(usuario.getNombre())
                .correoElectronico(usuario.getCorreoElectronico())
                .rol(usuario.getRol().name())
                .build();
    }
}
