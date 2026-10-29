package com.olsoftware.comerciantes.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tipo = "Bearer";
    private Long expiraEn; // milisegundos
    private String nombre;
    private String correoElectronico;
    private String rol;
}
