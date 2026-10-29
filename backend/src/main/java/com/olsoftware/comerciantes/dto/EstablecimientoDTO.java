package com.olsoftware.comerciantes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstablecimientoDTO {

    private Long id;

    private Long comercianteId;

    @NotBlank(message = "El nombre del establecimiento es obligatorio")
    private String nombreEstablecimiento;

    @NotNull(message = "Los ingresos son obligatorios")
    @DecimalMin(value = "0.0", inclusive = true, message = "Los ingresos deben ser un valor positivo o cero")
    private BigDecimal ingresos;

    @NotNull(message = "El número de empleados es obligatorio")
    @Min(value = 0, message = "El número de empleados debe ser mayor o igual a cero")
    private Integer numeroEmpleados;

    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;
}
