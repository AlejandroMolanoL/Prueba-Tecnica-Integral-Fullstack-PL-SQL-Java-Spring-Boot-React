package com.olsoftware.comerciantes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoComercianteDTO {

    @NotBlank(message = "El nuevo estado es obligatorio")
    @Pattern(regexp = "^(Registrado|Activo|Inactivo|Suspendido|Cancelado)$", 
             message = "El estado debe ser: Registrado, Activo, Inactivo, Suspendido o Cancelado")
    private String nuevoEstado;
}
