# Prueba Técnica Integral Fullstack (PL/SQL - Java Spring Boot - React)
## Agremiación Nacional de Comercio | OL Software

Solución completa y profesional desarrollada para la prueba técnica integral fullstack, estructurada rigurosamente en sus tres fases y carpetas correspondientes:

1. **`database/`** - Evaluación PL/SQL Fullstack (Oracle 11gR2 o superior)
2. **`backend/`** - Evaluación Spring Boot Fullstack (Java 21, Arquitectura Limpia, SOLID, JWT, Docker)
3. **`frontend/`** - Evaluación React Fullstack (React 19 / Vite, TypeScript, OWASP, Estado Global)

---

## 1. Fase Base de Datos: `database/`

Todos los scripts han sido creados en formato `.sql`, numerados secuencialmente para garantizar su correcta ejecución y replicación en ambientes de calificación:

- **`00_run_all.sql`**: Script maestro para ejecutar todo el flujo en Oracle SQL*Plus o SQL Developer.
- **`01_create_tables_and_indexes.sql`**: Creación de tablas (`COMERCIANTE`, `ESTABLECIMIENTO`, `DEPARTAMENTO`, `MUNICIPIO`), claves primarias, claves foráneas en cascada e índices funcionales (`UPPER`) y de filtros (`FECHA_REGISTRO`, `ESTADO`).
- **`02_sequences_and_triggers.sql`**: Secuencias de BD para claves primarias y Triggers `BEFORE INSERT` y `BEFORE UPDATE` para autogeneración de ID y actualización de campos de auditoría (`FECHA_ACTUALIZACION`, `USUARIO_ACTUALIZACION`).
- **`03_seed_data.sql`**: Semilla con exactamente 10 comerciantes y 30 establecimientos distribuidos aleatoriamente, además de catálogos de departamentos y municipios de Colombia.
- **`04_pkg_comerciantes_spec.sql`**: Especificación del paquete PL/SQL `PKG_COMERCIANTES`.
- **`05_pkg_comerciantes_body.sql`**: Cuerpo del paquete `PKG_COMERCIANTES`.
  - **Regla estricta:** Cero cursores implícitos (todos son cursores explícitos parametrizados).
  - Funciones `FN_CONSULTAR_POR_ID` y `FN_CONSULTAR_COMERCIANTES` (con paginación compatible 11gR2/12c+ y filtros).
  - Procedimientos `PR_CREAR_COMERCIANTE`, `PR_ACTUALIZAR_COMERCIANTE`, `PR_CAMBIAR_ESTADO_COMERCIANTE` y `PR_ELIMINAR_COMERCIANTE` con parámetros de salida `P_CODIGO_ERROR` (0 éxito) y `P_MENSAJE_ERROR`.
  - Función `FN_REPORTE_COMERCIANTES_ACTIVOS` que retorna cursor referenciado ordenado descendentemente por cantidad de establecimientos.
- **`06_mer_diagram.md`**: Diagrama Entidad-Relación (MER) en Mermaid, diccionario de datos y justificación de formas normales (1FN, 2FN, 3FN).

---

## 2. Fase Backend: `backend/`

Construido con **Java 21 LTS** y **Spring Boot 3.3.5**, siguiendo Arquitectura Limpia y principios SOLID.

### Características Principales:
- **Seguridad y JWT (Reto 06):** Endpoint público de login (`POST /api/v1/auth/login`), emisión de JWT con expiración de 1 hora, control de acceso por roles (`ADMINISTRADOR` y `AUXILIAR_REGISTRO`), política CORS configurada.
- **Listas de Valores con Caché (Reto 07):** Endpoints cacheados en memoria con `@Cacheable` para Departamentos y Municipios, evitando consultas redundantes a la base de datos.
- **CRUD de Comerciantes (Reto 08):** Paginación por defecto de 5 registros, filtros por nombre, municipio, fecha y estado, consulta por ID con totales agregados en tiempo real, actualización de estado mediante HTTP `PATCH /api/v1/comerciantes/{id}/estado`.
- **CRUD de Establecimientos (Reto 09):** Operaciones CRUD completas asociadas al comerciante.
- **Reporte CSV delimitado por pipe (Reto 10):** Generación de archivo plano `.csv` separado por `|`, exclusivo para rol Administrador.
- **Ficha del Comerciante en PDF (Reto 11):** Generación de documento PDF estructurado y estilizado profesionalmente usando OpenPDF, exclusivo para rol Administrador.
- **Pruebas Unitarias:** 10 pruebas unitarias con JUnit 5 y Mockito (`AuthServiceTest`, `ComercianteServiceTest`, `ReporteServiceTest`).
- **Documentación y Colección:**
  - Swagger UI interactivo: `http://localhost:8080/swagger-ui.html`
  - OpenAPI Spec: `http://localhost:8080/v3/api-docs`
  - Colección de Postman: `backend/postman_collection.json`
- **Despliegue Contenerizado:** `Dockerfile` multi-etapa y `docker-compose.yml`.

### Credenciales de Prueba Precargadas:
| Rol | Correo Electrónico | Contraseña | Permisos |
|---|---|---|---|
| **Administrador** | `admin@olsoftware.com` | `Admin123*` | Acceso total: CRUD, Eliminar, Reporte CSV, Ficha PDF |
| **Auxiliar de Registro** | `auxiliar@olsoftware.com` | `Auxiliar123*` | Consulta, Creación, Edición y Activación/Inactivación |

### Ejecución del Backend:
```bash
# Entrar a la carpeta backend
cd backend

# Ejecutar las pruebas unitarias
.tools\apache-maven-3.9.9\bin\mvn.cmd clean test

# Iniciar la aplicación
.tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

---

## 3. Fase Frontend: `frontend/`

Desarrollado con **React 19 / Vite**, **TypeScript** y diseño fiel a los prototipos de la prueba técnica.

### Características Principales:
- **Gestión de Estado Global:** `AuthContext` para administración de sesión de usuario, token JWT y roles; `ToastContext` para feedback interactivo en pantalla.
- **Seguridad OWASP (Reto 12):** Almacenamiento seguro en `sessionStorage` (sin exposición en `localStorage` o `window`), ofuscación/sanitización en tránsito, protección contra ataques XSS y CSRF.
- **Página de Login (Reto 12):** Prototipo fiel con campos obligatorios, validación de checkbox "Acepto términos y condiciones" requerido para habilitar el botón "Iniciar sesión", y botones de acceso rápido para probar con rol Administrador y Auxiliar.
- **Página Home (Reto 13):**
  - Encabezado con logo OL Software, usuario autenticado, rol y botón de cerrar sesión.
  - Botón magenta "Crear Formulario Nuevo" y botón "Descargar Reporte en CSV".
  - Filtros reactivos por Nombre/Razón Social, Municipio, Fecha de Registro y Estado.
  - Tabla con todas las columnas solicitadas y botones de acción contextuales según rol (Editar, Activar/Inactivar con PATCH, Eliminar [solo Admin], Descargar Ficha PDF [solo Admin]).
  - Paginador con selector de items por página (5, 10, 20) y controles de página idénticos al prototipo.
- **Página Formulario de Creación y Edición (Reto 14):**
  - Datos Generales con selectores en cascada de Departamento y Municipio consumiendo la API de listas de valores.
  - Checkbox "¿Posee establecimientos?". Al marcarlo, despliega dinámicamente la sección "Gestionar Establecimiento" con tarjetas de locales y modal de creación/edición.
  - Footer flotante fijo en la parte inferior con sumatoria en vivo de Ingresos y Empleados, junto con el botón de envío.
- **Pruebas Unitarias:** 6 pruebas unitarias con Vitest y Testing Library (`LoginPage.test.tsx`, `Navbar.test.tsx`, `AuthService.test.ts`).
- **Compilado de Producción Comprimido:**
  - Carpeta `frontend/dist/` generada con `yarn build`.
  - Archivo comprimido listo para entrega: **`frontend/frontend-dist.zip`**.

### Ejecución del Frontend:
```bash
# Entrar a la carpeta frontend
cd frontend

# Ejecutar pruebas unitarias
yarn test

# Iniciar servidor de desarrollo
yarn dev
```
Acceder en el navegador a: `http://localhost:5173`
