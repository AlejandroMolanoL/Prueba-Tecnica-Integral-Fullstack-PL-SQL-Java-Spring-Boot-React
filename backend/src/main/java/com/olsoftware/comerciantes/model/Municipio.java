package com.olsoftware.comerciantes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MUNICIPIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MUNICIPIO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEPARTAMENTO", nullable = false)
    @JsonIgnore
    private Departamento departamento;

    @Column(name = "CODIGO_DANE", nullable = false, unique = true, length = 10)
    private String codigoDane;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "FECHA_ACTUALIZACION")
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "USUARIO_ACTUALIZACION", length = 50)
    @Builder.Default
    private String usuarioActualizacion = "SYSTEM";
}
