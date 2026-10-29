package com.olsoftware.comerciantes.config;

import com.olsoftware.comerciantes.model.*;
import com.olsoftware.comerciantes.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;
    private final ComercianteRepository comercianteRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsuarios();
        initDepartamentosYMunicipios();
        initComerciantesYEstablecimientos();
    }

    private void initUsuarios() {
        if (usuarioRepository.count() == 0) {
            log.info("Inicializando usuarios de prueba...");

            Usuario admin = Usuario.builder()
                    .nombre("Administrador Principal")
                    .correoElectronico("admin@olsoftware.com")
                    .contrasena(passwordEncoder.encode("Admin123*"))
                    .rol(Rol.ADMINISTRADOR)
                    .activo(true)
                    .build();

            Usuario auxiliar = Usuario.builder()
                    .nombre("Auxiliar de Registro")
                    .correoElectronico("auxiliar@olsoftware.com")
                    .contrasena(passwordEncoder.encode("Auxiliar123*"))
                    .rol(Rol.AUXILIAR_REGISTRO)
                    .activo(true)
                    .build();

            usuarioRepository.saveAll(List.of(admin, auxiliar));
            log.info("Usuarios creados: admin@olsoftware.com y auxiliar@olsoftware.com");
        }
    }

    private void initDepartamentosYMunicipios() {
        if (departamentoRepository.count() == 0) {
            log.info("Inicializando lista de valores de Departamentos y Municipios...");

            Departamento antioquia = Departamento.builder().codigoDane("05").nombre("Antioquia").build();
            Departamento bogota = Departamento.builder().codigoDane("11").nombre("Bogotá D.C.").build();
            Departamento valle = Departamento.builder().codigoDane("76").nombre("Valle del Cauca").build();
            Departamento atlantico = Departamento.builder().codigoDane("08").nombre("Atlántico").build();
            Departamento santander = Departamento.builder().codigoDane("68").nombre("Santander").build();
            Departamento amazonas = Departamento.builder().codigoDane("91").nombre("Amazonas").build();

            departamentoRepository.saveAll(List.of(antioquia, bogota, valle, atlantico, santander, amazonas));

            municipioRepository.saveAll(List.of(
                    Municipio.builder().departamento(antioquia).codigoDane("05001").nombre("Medellín").build(),
                    Municipio.builder().departamento(antioquia).codigoDane("05088").nombre("Bello").build(),
                    Municipio.builder().departamento(antioquia).codigoDane("05266").nombre("Envigado").build(),
                    Municipio.builder().departamento(antioquia).codigoDane("05360").nombre("Itagüí").build(),
                    Municipio.builder().departamento(bogota).codigoDane("11001").nombre("Bogotá").build(),
                    Municipio.builder().departamento(valle).codigoDane("76001").nombre("Cali").build(),
                    Municipio.builder().departamento(valle).codigoDane("76520").nombre("Palmira").build(),
                    Municipio.builder().departamento(valle).codigoDane("76834").nombre("Tuluá").build(),
                    Municipio.builder().departamento(atlantico).codigoDane("08001").nombre("Barranquilla").build(),
                    Municipio.builder().departamento(atlantico).codigoDane("08758").nombre("Soledad").build(),
                    Municipio.builder().departamento(santander).codigoDane("68001").nombre("Bucaramanga").build(),
                    Municipio.builder().departamento(santander).codigoDane("68276").nombre("Floridablanca").build(),
                    Municipio.builder().departamento(amazonas).codigoDane("91001").nombre("Leticia").build(),
                    Municipio.builder().departamento(amazonas).codigoDane("91263").nombre("El Encanto").build()
            ));
        }
    }

    private void initComerciantesYEstablecimientos() {
        if (comercianteRepository.count() == 0) {
            log.info("Inicializando datos semilla (10 comerciantes y 30 establecimientos)...");

            Comerciante c1 = Comerciante.builder()
                    .nombreRazonSocial("Inversiones Los Andes S.A.S.")
                    .departamento("Antioquia")
                    .municipio("Medellín")
                    .telefono("+57 123 456 7890")
                    .correoElectronico("contacto@losandes.com")
                    .fechaRegistro(LocalDate.of(2024, 1, 15))
                    .estado("Activo")
                    .build();

            Comerciante c2 = Comerciante.builder()
                    .nombreRazonSocial("Distribuidora del Caribe Ltda.")
                    .departamento("Atlántico")
                    .municipio("Barranquilla")
                    .telefono("+57 098 765 4321")
                    .correoElectronico("ventas@distcaribe.co")
                    .fechaRegistro(LocalDate.of(2024, 2, 10))
                    .estado("Activo")
                    .build();

            Comerciante c3 = Comerciante.builder()
                    .nombreRazonSocial("Comercializadora Pacífico Sur")
                    .departamento("Valle del Cauca")
                    .municipio("Cali")
                    .telefono("+57 321 654 9870")
                    .correoElectronico("gerencia@pacificosur.com")
                    .fechaRegistro(LocalDate.of(2024, 3, 5))
                    .estado("Activo")
                    .build();

            Comerciante c4 = Comerciante.builder()
                    .nombreRazonSocial("Tecnología Central Colombia")
                    .departamento("Bogotá D.C.")
                    .municipio("Bogotá")
                    .telefono("+57 567 890 1234")
                    .correoElectronico("info@tecnocentral.com.co")
                    .fechaRegistro(LocalDate.of(2024, 3, 20))
                    .estado("Registrado")
                    .build();

            Comerciante c5 = Comerciante.builder()
                    .nombreRazonSocial("Agroinsumos del Oriente S.A.")
                    .departamento("Santander")
                    .municipio("Bucaramanga")
                    .telefono("+57 234 567 8901")
                    .correoElectronico("servicio@agrooriente.com")
                    .fechaRegistro(LocalDate.of(2024, 4, 12))
                    .estado("Inactivo")
                    .build();

            Comerciante c6 = Comerciante.builder()
                    .nombreRazonSocial("Logística & Envíos Valle")
                    .departamento("Valle del Cauca")
                    .municipio("Palmira")
                    .telefono("+57 345 678 9012")
                    .correoElectronico("logistica@enviosvalle.com")
                    .fechaRegistro(LocalDate.of(2024, 5, 1))
                    .estado("Activo")
                    .build();

            Comerciante c7 = Comerciante.builder()
                    .nombreRazonSocial("Supermercados La Selva")
                    .departamento("Amazonas")
                    .municipio("Leticia")
                    .telefono("+57 456 789 0123")
                    .correoElectronico("admin@superlaselva.com")
                    .fechaRegistro(LocalDate.of(2024, 5, 18))
                    .estado("Suspendido")
                    .build();

            Comerciante c8 = Comerciante.builder()
                    .nombreRazonSocial("Almacenes El Éxito Paisa")
                    .departamento("Antioquia")
                    .municipio("Envigado")
                    .telefono("+57 567 890 1234")
                    .correoElectronico("contacto@exitopaisa.com")
                    .fechaRegistro(LocalDate.of(2024, 6, 2))
                    .estado("Activo")
                    .build();

            Comerciante c9 = Comerciante.builder()
                    .nombreRazonSocial("Constructora Metropolitana")
                    .departamento("Bogotá D.C.")
                    .municipio("Bogotá")
                    .telefono("+57 678 901 2345")
                    .correoElectronico("proyectos@metroconstruc.com")
                    .fechaRegistro(LocalDate.of(2024, 6, 25))
                    .estado("Cancelado")
                    .build();

            Comerciante c10 = Comerciante.builder()
                    .nombreRazonSocial("Moda y Calzado Santandereano")
                    .departamento("Santander")
                    .municipio("Floridablanca")
                    .telefono("+57 789 012 3456")
                    .correoElectronico("ventas@modasantander.com")
                    .fechaRegistro(LocalDate.of(2024, 7, 10))
                    .estado("Registrado")
                    .build();

            List<Comerciante> guardados = comercianteRepository.saveAll(List.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10));

            // 30 Establecimientos distribuidos entre los comerciantes
            establecimientoRepository.saveAll(List.of(
                    // c1 (5)
                    Establecimiento.builder().comerciante(guardados.get(0)).nombreEstablecimiento("Sede Poblado Los Andes").ingresos(new BigDecimal("45000000.50")).numeroEmpleados(15).build(),
                    Establecimiento.builder().comerciante(guardados.get(0)).nombreEstablecimiento("Sucursal Laureles Los Andes").ingresos(new BigDecimal("32000000.00")).numeroEmpleados(10).build(),
                    Establecimiento.builder().comerciante(guardados.get(0)).nombreEstablecimiento("Centro Logístico Belén").ingresos(new BigDecimal("55000000.75")).numeroEmpleados(22).build(),
                    Establecimiento.builder().comerciante(guardados.get(0)).nombreEstablecimiento("Punto Express Sabaneta").ingresos(new BigDecimal("18500000.25")).numeroEmpleados(6).build(),
                    Establecimiento.builder().comerciante(guardados.get(0)).nombreEstablecimiento("Oficina Administrativa Alpujarra").ingresos(new BigDecimal("12000000.00")).numeroEmpleados(8).build(),
                    // c2 (4)
                    Establecimiento.builder().comerciante(guardados.get(1)).nombreEstablecimiento("Bodega Portuaria Barranquilla").ingresos(new BigDecimal("68000000.00")).numeroEmpleados(25).build(),
                    Establecimiento.builder().comerciante(guardados.get(1)).nombreEstablecimiento("Punto Norte Caribe").ingresos(new BigDecimal("24500000.50")).numeroEmpleados(9).build(),
                    Establecimiento.builder().comerciante(guardados.get(1)).nombreEstablecimiento("Distribuidora Soledad Centro").ingresos(new BigDecimal("19800000.00")).numeroEmpleados(7).build(),
                    Establecimiento.builder().comerciante(guardados.get(1)).nombreEstablecimiento("CEDI Vía 40").ingresos(new BigDecimal("82000000.80")).numeroEmpleados(30).build(),
                    // c3 (4)
                    Establecimiento.builder().comerciante(guardados.get(2)).nombreEstablecimiento("Comercial Granada Cali").ingresos(new BigDecimal("38000000.00")).numeroEmpleados(14).build(),
                    Establecimiento.builder().comerciante(guardados.get(2)).nombreEstablecimiento("Punto Ciudad Jardín").ingresos(new BigDecimal("41500000.40")).numeroEmpleados(12).build(),
                    Establecimiento.builder().comerciante(guardados.get(2)).nombreEstablecimiento("Bodega Yumbo Pacífico").ingresos(new BigDecimal("73000000.00")).numeroEmpleados(28).build(),
                    Establecimiento.builder().comerciante(guardados.get(2)).nombreEstablecimiento("Punto Express Chipichape").ingresos(new BigDecimal("22000000.20")).numeroEmpleados(8).build(),
                    // c4 (3)
                    Establecimiento.builder().comerciante(guardados.get(3)).nombreEstablecimiento("Showroom Tecnológico Chapinero").ingresos(new BigDecimal("52000000.00")).numeroEmpleados(18).build(),
                    Establecimiento.builder().comerciante(guardados.get(3)).nombreEstablecimiento("Laboratorio Reparaciones Calle 100").ingresos(new BigDecimal("36000000.60")).numeroEmpleados(14).build(),
                    Establecimiento.builder().comerciante(guardados.get(3)).nombreEstablecimiento("Tienda Unilago").ingresos(new BigDecimal("29000000.00")).numeroEmpleados(11).build(),
                    // c5 (2)
                    Establecimiento.builder().comerciante(guardados.get(4)).nombreEstablecimiento("Planta Insumos Bucaramanga").ingresos(new BigDecimal("47000000.90")).numeroEmpleados(19).build(),
                    Establecimiento.builder().comerciante(guardados.get(4)).nombreEstablecimiento("Distribución Agro Floridablanca").ingresos(new BigDecimal("21000000.00")).numeroEmpleados(8).build(),
                    // c6 (4)
                    Establecimiento.builder().comerciante(guardados.get(5)).nombreEstablecimiento("Base Operaciones Palmira").ingresos(new BigDecimal("35000000.00")).numeroEmpleados(16).build(),
                    Establecimiento.builder().comerciante(guardados.get(5)).nombreEstablecimiento("Terminal Carga Aeropuerto").ingresos(new BigDecimal("59000000.30")).numeroEmpleados(24).build(),
                    Establecimiento.builder().comerciante(guardados.get(5)).nombreEstablecimiento("Agencia Tuluá Envíos").ingresos(new BigDecimal("17000000.00")).numeroEmpleados(6).build(),
                    Establecimiento.builder().comerciante(guardados.get(5)).nombreEstablecimiento("Punto Recepción Buga").ingresos(new BigDecimal("14500000.50")).numeroEmpleados(5).build(),
                    // c7 (1)
                    Establecimiento.builder().comerciante(guardados.get(6)).nombreEstablecimiento("Hipermercado Leticia Centro").ingresos(new BigDecimal("28000000.00")).numeroEmpleados(15).build(),
                    // c8 (4)
                    Establecimiento.builder().comerciante(guardados.get(7)).nombreEstablecimiento("Tienda Principal Envigado").ingresos(new BigDecimal("49000000.10")).numeroEmpleados(20).build(),
                    Establecimiento.builder().comerciante(guardados.get(7)).nombreEstablecimiento("Punto Itagüí Éxito").ingresos(new BigDecimal("31000000.00")).numeroEmpleados(12).build(),
                    Establecimiento.builder().comerciante(guardados.get(7)).nombreEstablecimiento("Sucursal Bello Moda").ingresos(new BigDecimal("26500000.75")).numeroEmpleados(10).build(),
                    Establecimiento.builder().comerciante(guardados.get(7)).nombreEstablecimiento("Outlet Mayorista Guayabal").ingresos(new BigDecimal("39000000.00")).numeroEmpleados(15).build(),
                    // c9 (1)
                    Establecimiento.builder().comerciante(guardados.get(8)).nombreEstablecimiento("Campamento Obra Norte Bogotá").ingresos(new BigDecimal("95000000.00")).numeroEmpleados(45).build(),
                    // c10 (2)
                    Establecimiento.builder().comerciante(guardados.get(9)).nombreEstablecimiento("Boutique Cañaveral").ingresos(new BigDecimal("23000000.50")).numeroEmpleados(9).build(),
                    Establecimiento.builder().comerciante(guardados.get(9)).nombreEstablecimiento("Almacén Cabecera Bucaramanga").ingresos(new BigDecimal("31500000.00")).numeroEmpleados(11).build()
            ));

            log.info("Datos semilla creados con éxito: 10 comerciantes y 30 establecimientos.");
        }
    }
}
