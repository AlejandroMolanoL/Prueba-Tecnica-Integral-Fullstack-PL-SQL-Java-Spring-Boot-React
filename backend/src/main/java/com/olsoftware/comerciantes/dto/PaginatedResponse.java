package com.olsoftware.comerciantes.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> contenido;
    private int paginaActual;
    private int registrosPorPagina;
    private long totalRegistros;
    private int totalPaginas;
    private boolean esPrimera;
    private boolean esUltima;
}
