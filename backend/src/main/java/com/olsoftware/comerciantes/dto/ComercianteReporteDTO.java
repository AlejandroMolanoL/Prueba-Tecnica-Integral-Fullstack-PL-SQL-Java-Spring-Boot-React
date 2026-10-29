package com.olsoftware.comerciantes.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComercianteReporteDTO {
    private Long id;
    private String nombreRazonSocial;
    private String departamento;
    private String municipio;
    private String telefono;
    private String correoElectronico;
    private LocalDate fechaRegistro;
    private String estado;
    private Integer cantidadEstablecimientos;
    private BigDecimal totalActivos;
    private Integer cantidadEmpleados;
}
