package com.olsoftware.comerciantes.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMERCIANTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comerciante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMERCIANTE")
    private Long id;

    @Column(name = "NOMBRE_RAZON_SOCIAL", nullable = false, length = 200)
    private String nombreRazonSocial;

    @Column(name = "DEPARTAMENTO", nullable = false, length = 100)
    private String departamento;

    @Column(name = "MUNICIPIO", nullable = false, length = 100)
    private String municipio;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "CORREO_ELECTRONICO", length = 150)
    private String correoElectronico;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @OneToMany(mappedBy = "comerciante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("comerciante")
    @Builder.Default
    private List<Establecimiento> establecimientos = new ArrayList<>();

    @Column(name = "FECHA_ACTUALIZACION")
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "USUARIO_ACTUALIZACION", length = 50)
    @Builder.Default
    private String usuarioActualizacion = "SYSTEM";

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
