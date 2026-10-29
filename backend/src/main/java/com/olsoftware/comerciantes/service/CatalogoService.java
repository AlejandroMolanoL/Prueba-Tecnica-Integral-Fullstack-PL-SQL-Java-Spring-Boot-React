package com.olsoftware.comerciantes.service;

import com.olsoftware.comerciantes.dto.DepartamentoDTO;
import com.olsoftware.comerciantes.dto.MunicipioDTO;
import com.olsoftware.comerciantes.model.Departamento;
import com.olsoftware.comerciantes.model.Municipio;
import com.olsoftware.comerciantes.repository.DepartamentoRepository;
import com.olsoftware.comerciantes.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoService {

    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;

    @Cacheable(value = "departamentos")
    @Transactional(readOnly = true)
    public List<DepartamentoDTO> obtenerDepartamentos() {
        log.info("Consultando departamentos en base de datos (poblando caché)...");
        return departamentoRepository.findAllByOrderByNombreAsc().stream()
                .map(this::mapDepartamentoToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "municipios", key = "#departamentoId")
    @Transactional(readOnly = true)
    public List<MunicipioDTO> obtenerMunicipiosPorDepartamento(Long departamentoId) {
        log.info("Consultando municipios en base de datos para depto ID: {} (poblando caché)...", departamentoId);
        return municipioRepository.findByDepartamentoIdOrderByNombreAsc(departamentoId).stream()
                .map(this::mapMunicipioToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "municipiosPorNombre", key = "#nombreDepartamento")
    @Transactional(readOnly = true)
    public List<MunicipioDTO> obtenerMunicipiosPorNombreDepartamento(String nombreDepartamento) {
        log.info("Consultando municipios en base de datos para depto nombre: {} (poblando caché)...", nombreDepartamento);
        return municipioRepository.findByDepartamentoNombreIgnoreCaseOrderByNombreAsc(nombreDepartamento).stream()
                .map(this::mapMunicipioToDto)
                .collect(Collectors.toList());
    }

    private DepartamentoDTO mapDepartamentoToDto(Departamento d) {
        return DepartamentoDTO.builder()
                .id(d.getId())
                .codigoDane(d.getCodigoDane())
                .nombre(d.getNombre())
                .build();
    }

    private MunicipioDTO mapMunicipioToDto(Municipio m) {
        return MunicipioDTO.builder()
                .id(m.getId())
                .departamentoId(m.getDepartamento() != null ? m.getDepartamento().getId() : null)
                .codigoDane(m.getCodigoDane())
                .nombre(m.getNombre())
                .build();
    }
}
