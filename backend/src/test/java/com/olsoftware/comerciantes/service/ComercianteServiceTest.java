package com.olsoftware.comerciantes.service;

import com.olsoftware.comerciantes.dto.ComercianteDTO;
import com.olsoftware.comerciantes.dto.PaginatedResponse;
import com.olsoftware.comerciantes.exception.BusinessException;
import com.olsoftware.comerciantes.exception.ResourceNotFoundException;
import com.olsoftware.comerciantes.model.Comerciante;
import com.olsoftware.comerciantes.model.Establecimiento;
import com.olsoftware.comerciantes.repository.ComercianteRepository;
import com.olsoftware.comerciantes.repository.EstablecimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComercianteServiceTest {

    @Mock
    private ComercianteRepository comercianteRepository;

    @Mock
    private EstablecimientoRepository establecimientoRepository;

    @InjectMocks
    private ComercianteService comercianteService;

    private Comerciante comercianteMock;

    @BeforeEach
    void setUp() {
        comercianteMock = Comerciante.builder()
                .id(1L)
                .nombreRazonSocial("Comerciante Prueba S.A.S.")
                .departamento("Antioquia")
                .municipio("Medellín")
                .telefono("+57 300 123 4567")
                .correoElectronico("prueba@comercio.co")
                .fechaRegistro(LocalDate.of(2024, 1, 15))
                .estado("Activo")
                .establecimientos(new ArrayList<>())
                .build();

        Establecimiento est1 = Establecimiento.builder()
                .id(101L)
                .comerciante(comercianteMock)
                .nombreEstablecimiento("Sede Norte")
                .ingresos(new BigDecimal("50000000.00"))
                .numeroEmpleados(10)
                .build();

        Establecimiento est2 = Establecimiento.builder()
                .id(102L)
                .comerciante(comercianteMock)
                .nombreEstablecimiento("Sede Sur")
                .ingresos(new BigDecimal("25000000.50"))
                .numeroEmpleados(5)
                .build();

        comercianteMock.getEstablecimientos().add(est1);
        comercianteMock.getEstablecimientos().add(est2);
    }

    @Test
    @DisplayName("Debe consultar comerciante por ID calculando total de activos y empleados correctamente")
    void testObtenerPorIdConCalculos() {
        when(comercianteRepository.findById(1L)).thenReturn(Optional.of(comercianteMock));

        ComercianteDTO resultado = comercianteService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Comerciante Prueba S.A.S.", resultado.getNombreRazonSocial());
        assertEquals(2, resultado.getCantidadEstablecimientos());
        assertEquals(new BigDecimal("75000000.50"), resultado.getTotalActivos());
        assertEquals(15, resultado.getCantidadEmpleados());
        verify(comercianteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe listar comerciantes paginados con filtros")
    void testListarComerciantesPaginados() {
        Page<Comerciante> pageMock = new PageImpl<>(List.of(comercianteMock));
        when(comercianteRepository.findConFiltros(eq("Prueba"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(pageMock);

        PaginatedResponse<ComercianteDTO> response = comercianteService.listarComerciantesPaginados(
                "Prueba", null, null, null, 1, 5
        );

        assertNotNull(response);
        assertEquals(1, response.getContenido().size());
        assertEquals(1, response.getTotalRegistros());
        assertEquals("Comerciante Prueba S.A.S.", response.getContenido().get(0).getNombreRazonSocial());
    }

    @Test
    @DisplayName("Debe cambiar estado del comerciante (PATCH)")
    void testCambiarEstado() {
        when(comercianteRepository.findById(1L)).thenReturn(Optional.of(comercianteMock));
        when(comercianteRepository.save(any(Comerciante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComercianteDTO resultado = comercianteService.cambiarEstado(1L, "Inactivo");

        assertNotNull(resultado);
        assertEquals("Inactivo", resultado.getEstado());
        verify(comercianteRepository).save(any(Comerciante.class));
    }

    @Test
    @DisplayName("Debe arrojar BusinessException si el estado es inválido")
    void testCambiarEstadoInvalido() {
        assertThrows(BusinessException.class, () -> comercianteService.cambiarEstado(1L, "EstadoDesconocido"));
    }

    @Test
    @DisplayName("Debe arrojar ResourceNotFoundException si comerciante no existe")
    void testComercianteNoExiste() {
        when(comercianteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> comercianteService.obtenerPorId(999L));
    }
}
