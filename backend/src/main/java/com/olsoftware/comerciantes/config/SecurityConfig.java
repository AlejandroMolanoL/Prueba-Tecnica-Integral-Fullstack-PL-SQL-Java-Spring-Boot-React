package com.olsoftware.comerciantes.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(ex -> ex
                        // Retornar 401 cuando no hay token o es inválido (en lugar de 403 por defecto)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"exito\":false,\"mensaje\":\"No autorizado: Se requiere token de autenticación válido para acceder a este recurso.\"}");
                        })
                        // Retornar 403 cuando el usuario no tiene el rol necesario
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"exito\":false,\"mensaje\":\"Acceso denegado: Su rol actual no tiene permisos suficientes para realizar esta acción.\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Permitir todas las peticiones preflight CORS (OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Endpoints públicos
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/h2-console/**",
                                "/actuator/**"
                        ).permitAll()
                        
                        // Reto 10: Reporte CSV accesible solo a Administrador
                        .requestMatchers("/api/v1/comerciantes/reporte/csv").hasRole("ADMINISTRADOR")
                        
                        // Reto 11: Ficha del comerciante en PDF accesible solo a Administrador
                        .requestMatchers("/api/v1/comerciantes/*/ficha/pdf").hasRole("ADMINISTRADOR")
                        
                        // Reto 13: Eliminar comerciante accesible solo a Administrador
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/comerciantes/**").hasRole("ADMINISTRADOR")
                        
                        // Demás endpoints de comerciantes y establecimientos (Admin y Auxiliar de Registro)
                        .requestMatchers("/api/v1/comerciantes/**").hasAnyRole("ADMINISTRADOR", "AUXILIAR_REGISTRO")
                        .requestMatchers("/api/v1/establecimientos/**").hasAnyRole("ADMINISTRADOR", "AUXILIAR_REGISTRO")
                        .requestMatchers("/api/v1/departamentos", "/api/v1/departamentos/**").hasAnyRole("ADMINISTRADOR", "AUXILIAR_REGISTRO")
                        .requestMatchers("/api/v1/municipios", "/api/v1/municipios/**").hasAnyRole("ADMINISTRADOR", "AUXILIAR_REGISTRO")
                        
                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
