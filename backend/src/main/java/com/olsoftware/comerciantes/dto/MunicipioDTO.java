package com.olsoftware.comerciantes.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MunicipioDTO {
    private Long id;
    private Long departamentoId;
    private String codigoDane;
    private String nombre;
}
