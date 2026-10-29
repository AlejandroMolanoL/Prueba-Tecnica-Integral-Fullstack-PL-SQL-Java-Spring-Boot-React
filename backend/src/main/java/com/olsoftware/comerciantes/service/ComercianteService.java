package com.olsoftware.comerciantes.service;

import com.olsoftware.comerciantes.dto.*;
import com.olsoftware.comerciantes.exception.BusinessException;
import com.olsoftware.comerciantes.exception.ResourceNotFoundException;
import com.olsoftware.comerciantes.model.Comerciante;
import com.olsoftware.comerciantes.model.Establecimiento;
import com.olsoftware.comerciantes.repository.ComercianteRepository;
import com.olsoftware.comerciantes.repository.EstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComercianteService {

    private final ComercianteRepository comercianteRepository;
    private final EstablecimientoRepository establecimientoRepository;

    private String getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ComercianteDTO> listarComerciantesPaginados(
            String nombre,
            String municipio,
            LocalDate fechaRegistro,
            String estado,
            int pagina,
            int registrosPorPagina
    ) {
        int pageNumber = Math.max(pagina - 1, 0); // JPA es 0-indexed
        int pageSize = registrosPorPagina > 0 ? registrosPorPagina : 5; // 5 por defecto

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Comerciante> resultado = comercianteRepository.findConFiltros(
                nombre != null && !nombre.isBlank() ? nombre.trim() : null,
                municipio != null && !municipio.isBlank() ? municipio.trim() : null,
                fechaRegistro,
                estado != null && !estado.isBlank() ? estado.trim() : null,
                pageable
        );

        List<ComercianteDTO> contenido = resultado.getContent().stream()
                .map(this::mapToDtoConCalculos)
                .collect(Collectors.toList());

        return PaginatedResponse.<ComercianteDTO>builder()
                .contenido(contenido)
                .paginaActual(resultado.getNumber() + 1)
                .registrosPorPagina(resultado.getSize())
                .totalRegistros(resultado.getTotalElements())
                .totalPaginas(resultado.getTotalPages())
                .esPrimera(resultado.isFirst())
                .esUltima(resultado.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public ComercianteDTO obtenerPorId(Long id) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + id));
        return mapToDtoConCalculos(comerciante);
    }

    @Transactional
    public ComercianteDTO crear(ComercianteCreateDTO dto) {
        validarEstado(dto.getEstado());

        Comerciante comerciante = Comerciante.builder()
                .nombreRazonSocial(dto.getNombreRazonSocial().trim())
                .departamento(dto.getDepartamento().trim())
                .municipio(dto.getMunicipio().trim())
                .telefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null)
                .correoElectronico(dto.getCorreoElectronico() != null ? dto.getCorreoElectronico().trim().toLowerCase() : null)
                .fechaRegistro(dto.getFechaRegistro())
                .estado(dto.getEstado().trim())
                .fechaActualizacion(LocalDateTime.now())
                .usuarioActualizacion(getUsuarioActual())
                .build();

        Comerciante guardado = comercianteRepository.save(comerciante);

        // Si se enviaron establecimientos en la creación
        if (dto.getEstablecimientos() != null && !dto.getEstablecimientos().isEmpty()) {
            List<Establecimiento> listaEstablecimientos = new ArrayList<>();
            for (EstablecimientoDTO estDto : dto.getEstablecimientos()) {
                Establecimiento est = Establecimiento.builder()
                        .comerciante(guardado)
                        .nombreEstablecimiento(estDto.getNombreEstablecimiento().trim())
                        .ingresos(estDto.getIngresos() != null ? estDto.getIngresos() : BigDecimal.ZERO)
                        .numeroEmpleados(estDto.getNumeroEmpleados() != null ? estDto.getNumeroEmpleados() : 0)
                        .usuarioActualizacion(getUsuarioActual())
                        .fechaActualizacion(LocalDateTime.now())
                        .build();
                listaEstablecimientos.add(est);
            }
            establecimientoRepository.saveAll(listaEstablecimientos);
            guardado.setEstablecimientos(listaEstablecimientos);
        }

        return mapToDtoConCalculos(guardado);
    }

    @Transactional
    public ComercianteDTO actualizar(Long id, ComercianteUpdateDTO dto) {
        validarEstado(dto.getEstado());

        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + id));

        comerciante.setNombreRazonSocial(dto.getNombreRazonSocial().trim());
        comerciante.setDepartamento(dto.getDepartamento().trim());
        comerciante.setMunicipio(dto.getMunicipio().trim());
        comerciante.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        comerciante.setCorreoElectronico(dto.getCorreoElectronico() != null ? dto.getCorreoElectronico().trim().toLowerCase() : null);
        comerciante.setFechaRegistro(dto.getFechaRegistro());
        comerciante.setEstado(dto.getEstado().trim());
        comerciante.setFechaActualizacion(LocalDateTime.now());
        comerciante.setUsuarioActualizacion(getUsuarioActual());

        Comerciante actualizado = comercianteRepository.save(comerciante);
        return mapToDtoConCalculos(actualizado);
    }

    @Transactional
    public ComercianteDTO cambiarEstado(Long id, String nuevoEstado) {
        validarEstado(nuevoEstado);

        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + id));

        comerciante.setEstado(nuevoEstado.trim());
        comerciante.setFechaActualizacion(LocalDateTime.now());
        comerciante.setUsuarioActualizacion(getUsuarioActual());

        Comerciante actualizado = comercianteRepository.save(comerciante);
        return mapToDtoConCalculos(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!comercianteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comerciante no encontrado con ID: " + id);
        }
        comercianteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ComercianteReporteDTO> obtenerReporteComerciantesActivos() {
        List<Comerciante> activos = comercianteRepository.findComerciantesRegistradosYActivosOrdenadosPorEstablecimientos();

        return activos.stream().map(c -> {
            List<Establecimiento> ests = c.getEstablecimientos() != null ? c.getEstablecimientos() : List.of();
            BigDecimal totalActivos = ests.stream()
                    .map(Establecimiento::getIngresos)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalEmpleados = ests.stream()
                    .mapToInt(Establecimiento::getNumeroEmpleados)
                    .sum();

            return ComercianteReporteDTO.builder()
                    .id(c.getId())
                    .nombreRazonSocial(c.getNombreRazonSocial())
                    .departamento(c.getDepartamento())
                    .municipio(c.getMunicipio())
                    .telefono(c.getTelefono())
                    .correoElectronico(c.getCorreoElectronico())
                    .fechaRegistro(c.getFechaRegistro())
                    .estado(c.getEstado())
                    .cantidadEstablecimientos(ests.size())
                    .totalActivos(totalActivos)
                    .cantidadEmpleados(totalEmpleados)
                    .build();
        }).collect(Collectors.toList());
    }

    private void validarEstado(String estado) {
        if (estado == null || !List.of("Registrado", "Activo", "Inactivo", "Suspendido", "Cancelado").contains(estado.trim())) {
            throw new BusinessException("El estado debe ser: Registrado, Activo, Inactivo, Suspendido o Cancelado");
        }
    }

    public ComercianteDTO mapToDtoConCalculos(Comerciante c) {
        List<Establecimiento> establecimientos = c.getEstablecimientos() != null ? c.getEstablecimientos() : List.of();

        BigDecimal totalActivos = establecimientos.stream()
                .map(Establecimiento::getIngresos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalEmpleados = establecimientos.stream()
                .mapToInt(Establecimiento::getNumeroEmpleados)
                .sum();

        List<EstablecimientoDTO> estDtos = establecimientos.stream()
                .map(e -> EstablecimientoDTO.builder()
                        .id(e.getId())
                        .comercianteId(c.getId())
                        .nombreEstablecimiento(e.getNombreEstablecimiento())
                        .ingresos(e.getIngresos())
                        .numeroEmpleados(e.getNumeroEmpleados())
                        .fechaActualizacion(e.getFechaActualizacion())
                        .usuarioActualizacion(e.getUsuarioActualizacion())
                        .build())
                .collect(Collectors.toList());

        return ComercianteDTO.builder()
                .id(c.getId())
                .nombreRazonSocial(c.getNombreRazonSocial())
                .departamento(c.getDepartamento())
                .municipio(c.getMunicipio())
                .telefono(c.getTelefono())
                .correoElectronico(c.getCorreoElectronico())
                .fechaRegistro(c.getFechaRegistro())
                .estado(c.getEstado())
                .cantidadEstablecimientos(establecimientos.size())
                .totalActivos(totalActivos)
                .cantidadEmpleados(totalEmpleados)
                .establecimientos(estDtos)
                .fechaActualizacion(c.getFechaActualizacion())
                .usuarioActualizacion(c.getUsuarioActualizacion())
                .build();
    }
}
