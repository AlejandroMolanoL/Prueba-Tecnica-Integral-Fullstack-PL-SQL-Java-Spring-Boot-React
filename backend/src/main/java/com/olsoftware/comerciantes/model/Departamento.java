package com.olsoftware.comerciantes.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DEPARTAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DEPARTAMENTO")
    private Long id;

    @Column(name = "CODIGO_DANE", nullable = false, unique = true, length = 10)
    private String codigoDane;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @OneToMany(mappedBy = "departamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("departamento")
    @Builder.Default
    private List<Municipio> municipios = new ArrayList<>();

    @Column(name = "FECHA_ACTUALIZACION")
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "USUARIO_ACTUALIZACION", length = 50)
    @Builder.Default
    private String usuarioActualizacion = "SYSTEM";
}
