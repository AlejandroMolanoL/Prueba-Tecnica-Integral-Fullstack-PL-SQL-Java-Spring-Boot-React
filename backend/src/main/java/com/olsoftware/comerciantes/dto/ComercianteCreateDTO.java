package com.olsoftware.comerciantes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComercianteCreateDTO {

    @NotBlank(message = "El nombre o razón social es obligatorio")
    private String nombreRazonSocial;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "El municipio o ciudad es obligatorio")
    private String municipio;

    @Pattern(regexp = "^$|^[+]?[0-9\\s-]{7,20}$", message = "El formato de teléfono no es válido")
    private String telefono;

    @Email(message = "El formato de correo electrónico no es válido")
    private String correoElectronico;

    @NotNull(message = "La fecha de registro es obligatoria")
    private LocalDate fechaRegistro;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(Registrado|Activo|Inactivo|Suspendido|Cancelado)$", 
             message = "El estado debe ser: Registrado, Activo, Inactivo, Suspendido o Cancelado")
    private String estado;

    // Establecimientos opcionales en creación simultánea
    private List<EstablecimientoDTO> establecimientos;
}
