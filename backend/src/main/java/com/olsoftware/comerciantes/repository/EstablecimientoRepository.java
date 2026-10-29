package com.olsoftware.comerciantes.repository;

import com.olsoftware.comerciantes.model.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Long> {
    List<Establecimiento> findByComercianteId(Long comercianteId);
    void deleteByComercianteId(Long comercianteId);
}
