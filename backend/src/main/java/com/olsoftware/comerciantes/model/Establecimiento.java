package com.olsoftware.comerciantes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ESTABLECIMIENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ESTABLECIMIENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMERCIANTE", nullable = false)
    @JsonIgnore
    private Comerciante comerciante;

    @Column(name = "NOMBRE_ESTABLECIMIENTO", nullable = false, length = 200)
    private String nombreEstablecimiento;

    @Column(name = "INGRESOS", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal ingresos = BigDecimal.ZERO;

    @Column(name = "NUMERO_EMPLEADOS", nullable = false)
    @Builder.Default
    private Integer numeroEmpleados = 0;

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
