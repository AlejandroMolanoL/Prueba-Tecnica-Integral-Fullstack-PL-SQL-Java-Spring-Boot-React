package com.olsoftware.comerciantes.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartamentoDTO {
    private Long id;
    private String codigoDane;
    private String nombre;
    private List<MunicipioDTO> municipios;
}
