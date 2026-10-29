package com.olsoftware.comerciantes.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComercianteDTO {
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
    private List<EstablecimientoDTO> establecimientos;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;
}
