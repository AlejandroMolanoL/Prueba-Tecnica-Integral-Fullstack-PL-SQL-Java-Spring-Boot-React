package com.olsoftware.comerciantes.service;

import com.olsoftware.comerciantes.dto.ComercianteDTO;
import com.olsoftware.comerciantes.dto.ComercianteReporteDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ComercianteService comercianteService;

    @InjectMocks
    private ReporteService reporteService;

    @Test
    @DisplayName("Debe generar archivo CSV con delimitador pipe (|) y estructura requerida")
    void testGenerarReporteComerciantesCsv() {
        ComercianteReporteDTO dto = ComercianteReporteDTO.builder()
                .id(1L)
                .nombreRazonSocial("Comercial Los Andes")
                .departamento("Antioquia")
                .municipio("Medellín")
                .telefono("+57 300 123 4567")
                .correoElectronico("contacto@losandes.com")
                .fechaRegistro(LocalDate.of(2024, 1, 15))
                .estado("Activo")
                .cantidadEstablecimientos(3)
                .totalActivos(new BigDecimal("95000000.00"))
                .cantidadEmpleados(20)
                .build();

        when(comercianteService.obtenerReporteComerciantesActivos()).thenReturn(List.of(dto));

        byte[] csvBytes = reporteService.generarReporteComerciantesCsv();
        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        String contenido = new String(csvBytes);
        assertTrue(contenido.contains("|"));
        assertTrue(contenido.contains("Nombre o Razón Social|Departamento|Municipio"));
        assertTrue(contenido.contains("Comercial Los Andes|Antioquia|Medellín|+57 300 123 4567|contacto@losandes.com|2024-01-15|Activo|3|95000000.00|20"));
    }

    @Test
    @DisplayName("Debe generar documento PDF válido para la ficha del comerciante")
    void testGenerarFichaComerciantePdf() {
        ComercianteDTO comerciante = ComercianteDTO.builder()
                .id(1L)
                .nombreRazonSocial("Comercial Los Andes")
                .departamento("Antioquia")
                .municipio("Medellín")
                .telefono("+57 300 123 4567")
                .correoElectronico("contacto@losandes.com")
                .fechaRegistro(LocalDate.of(2024, 1, 15))
                .estado("Activo")
                .cantidadEstablecimientos(1)
                .totalActivos(new BigDecimal("50000000.00"))
                .cantidadEmpleados(10)
                .establecimientos(List.of())
                .build();

        when(comercianteService.obtenerPorId(1L)).thenReturn(comerciante);

        byte[] pdfBytes = reporteService.generarFichaComerciantePdf(1L);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Los archivos PDF inician con "%PDF-"
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", header);
    }
}
