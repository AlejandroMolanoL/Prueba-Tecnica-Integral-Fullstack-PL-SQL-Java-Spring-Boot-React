package com.olsoftware.comerciantes.repository;

import com.olsoftware.comerciantes.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    List<Departamento> findAllByOrderByNombreAsc();
    Optional<Departamento> findByCodigoDane(String codigoDane);
    Optional<Departamento> findByNombreIgnoreCase(String nombre);
}
