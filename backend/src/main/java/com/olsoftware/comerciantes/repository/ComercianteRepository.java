package com.olsoftware.comerciantes.repository;

import com.olsoftware.comerciantes.model.Comerciante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComercianteRepository extends JpaRepository<Comerciante, Long> {

    @Query("SELECT c FROM Comerciante c WHERE " +
           "(:nombre IS NULL OR UPPER(c.nombreRazonSocial) LIKE UPPER(CONCAT('%', :nombre, '%'))) AND " +
           "(:municipio IS NULL OR UPPER(c.municipio) = UPPER(:municipio)) AND " +
           "(:fechaRegistro IS NULL OR c.fechaRegistro = :fechaRegistro) AND " +
           "(:estado IS NULL OR UPPER(c.estado) = UPPER(:estado))")
    Page<Comerciante> findConFiltros(
            @Param("nombre") String nombre,
            @Param("municipio") String municipio,
            @Param("fechaRegistro") LocalDate fechaRegistro,
            @Param("estado") String estado,
            Pageable pageable
    );

    @Query("SELECT c FROM Comerciante c LEFT JOIN c.establecimientos e " +
           "WHERE c.estado IN ('Registrado', 'Activo') " +
           "GROUP BY c " +
           "ORDER BY COUNT(e) DESC, c.nombreRazonSocial ASC")
    List<Comerciante> findComerciantesRegistradosYActivosOrdenadosPorEstablecimientos();
}
