package com.olsoftware.comerciantes.service;

import com.olsoftware.comerciantes.dto.EstablecimientoDTO;
import com.olsoftware.comerciantes.exception.BusinessException;
import com.olsoftware.comerciantes.exception.ResourceNotFoundException;
import com.olsoftware.comerciantes.model.Comerciante;
import com.olsoftware.comerciantes.model.Establecimiento;
import com.olsoftware.comerciantes.repository.ComercianteRepository;
import com.olsoftware.comerciantes.repository.EstablecimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstablecimientoService {

    private final EstablecimientoRepository establecimientoRepository;
    private final ComercianteRepository comercianteRepository;

    private String getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoDTO> obtenerPorComerciante(Long comercianteId) {
        if (!comercianteRepository.existsById(comercianteId)) {
            throw new ResourceNotFoundException("Comerciante no encontrado con ID: " + comercianteId);
        }
        return establecimientoRepository.findByComercianteId(comercianteId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstablecimientoDTO obtenerPorId(Long id) {
        Establecimiento est = establecimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado con ID: " + id));
        return mapToDto(est);
    }

    @Transactional
    public EstablecimientoDTO crear(EstablecimientoDTO dto) {
        if (dto.getComercianteId() == null) {
            throw new BusinessException("El ID del comerciante es obligatorio para asociar el establecimiento");
        }

        Comerciante comerciante = comercianteRepository.findById(dto.getComercianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Comerciante no encontrado con ID: " + dto.getComercianteId()));

        Establecimiento establecimiento = Establecimiento.builder()
                .comerciante(comerciante)
                .nombreEstablecimiento(dto.getNombreEstablecimiento().trim())
                .ingresos(dto.getIngresos() != null ? dto.getIngresos() : BigDecimal.ZERO)
                .numeroEmpleados(dto.getNumeroEmpleados() != null ? dto.getNumeroEmpleados() : 0)
                .usuarioActualizacion(getUsuarioActual())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Establecimiento guardado = establecimientoRepository.save(establecimiento);
        return mapToDto(guardado);
    }

    @Transactional
    public EstablecimientoDTO actualizar(Long id, EstablecimientoDTO dto) {
        Establecimiento establecimiento = establecimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Establecimiento no encontrado con ID: " + id));

        establecimiento.setNombreEstablecimiento(dto.getNombreEstablecimiento().trim());
        establecimiento.setIngresos(dto.getIngresos() != null ? dto.getIngresos() : BigDecimal.ZERO);
        establecimiento.setNumeroEmpleados(dto.getNumeroEmpleados() != null ? dto.getNumeroEmpleados() : 0);
        establecimiento.setUsuarioActualizacion(getUsuarioActual());
        establecimiento.setFechaActualizacion(LocalDateTime.now());

        Establecimiento actualizado = establecimientoRepository.save(establecimiento);
        return mapToDto(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!establecimientoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Establecimiento no encontrado con ID: " + id);
        }
        establecimientoRepository.deleteById(id);
    }

    public EstablecimientoDTO mapToDto(Establecimiento e) {
        return EstablecimientoDTO.builder()
                .id(e.getId())
                .comercianteId(e.getComerciante() != null ? e.getComerciante().getId() : null)
                .nombreEstablecimiento(e.getNombreEstablecimiento())
                .ingresos(e.getIngresos())
                .numeroEmpleados(e.getNumeroEmpleados())
                .fechaActualizacion(e.getFechaActualizacion())
                .usuarioActualizacion(e.getUsuarioActualizacion())
                .build();
    }
}
