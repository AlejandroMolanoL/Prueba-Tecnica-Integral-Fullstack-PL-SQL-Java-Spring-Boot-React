-- ====================================================================
-- EVALUACIÓN PL/SQL FULLSTACK - RETO 04 Y RETO 05
-- SCRIPT 04: ESPECIFICACIÓN DEL PAQUETE PKG_COMERCIANTES
-- Motor: Oracle 11gR2 o superior
-- ====================================================================

CREATE OR REPLACE PACKAGE PKG_COMERCIANTES AS

    -- ----------------------------------------------------------------
    -- TIPOS DE DATOS Y RECORDS DEL PAQUETE
    -- ----------------------------------------------------------------
    
    -- Record con la estructura solicitada para consultas de comerciantes
    TYPE T_REGISTRO_COMERCIANTE IS RECORD (
        ID_COMERCIANTE        NUMBER(10),
        NOMBRE_RAZON_SOCIAL   VARCHAR2(200),
        DEPARTAMENTO          VARCHAR2(100),
        MUNICIPIO             VARCHAR2(100),
        TELEFONO              VARCHAR2(20),
        CORREO_ELECTRONICO    VARCHAR2(150),
        FECHA_REGISTRO        DATE,
        ESTADO                VARCHAR2(20),
        TOTAL_ACTIVOS         NUMBER(15, 2),
        CANTIDAD_EMPLEADOS    NUMBER(10)
    );

    -- Cursor referenciado débil (SYS_REFCURSOR)
    TYPE T_CURSOR_COMERCIANTES IS REF CURSOR;

    -- Cursor tipado para reporte (Reto 05)
    TYPE T_REGISTRO_REPORTE IS RECORD (
        ID_COMERCIANTE            NUMBER(10),
        NOMBRE_RAZON_SOCIAL       VARCHAR2(200),
        DEPARTAMENTO              VARCHAR2(100),
        MUNICIPIO                 VARCHAR2(100),
        TELEFONO                  VARCHAR2(20),
        CORREO_ELECTRONICO        VARCHAR2(150),
        FECHA_REGISTRO            DATE,
        ESTADO                    VARCHAR2(20),
        CANTIDAD_ESTABLECIMIENTOS NUMBER(10),
        TOTAL_ACTIVOS             NUMBER(15, 2),
        CANTIDAD_EMPLEADOS        NUMBER(10)
    );

    -- ----------------------------------------------------------------
    -- RETO 04: OPERACIONES DE CONSULTA (FUNCIONES)
    -- ----------------------------------------------------------------

    -- Función para consultar un comerciante por su ID
    -- Retorna cursor explícito con estructura detallada
    FUNCTION FN_CONSULTAR_POR_ID (
        P_ID_COMERCIANTE IN NUMBER
    ) RETURN SYS_REFCURSOR;

    -- Función para consultar comerciantes con filtros y paginación
    -- Filtros: Nombre o Razón Social, Municipio, Fecha Registro, Estado
    -- Retorna cursor explícito con paginación
    FUNCTION FN_CONSULTAR_COMERCIANTES (
        P_NOMBRE_RAZON_SOCIAL IN VARCHAR2 DEFAULT NULL,
        P_MUNICIPIO           IN VARCHAR2 DEFAULT NULL,
        P_FECHA_REGISTRO      IN DATE     DEFAULT NULL,
        P_ESTADO              IN VARCHAR2 DEFAULT NULL,
        P_PAGINA              IN NUMBER   DEFAULT 1,
        P_REGISTROS_POR_PAG   IN NUMBER   DEFAULT 5
    ) RETURN SYS_REFCURSOR;

    -- Función auxiliar para contar el total de registros filtrados (soporte para paginación)
    FUNCTION FN_CONTAR_COMERCIANTES (
        P_NOMBRE_RAZON_SOCIAL IN VARCHAR2 DEFAULT NULL,
        P_MUNICIPIO           IN VARCHAR2 DEFAULT NULL,
        P_FECHA_REGISTRO      IN DATE     DEFAULT NULL,
        P_ESTADO              IN VARCHAR2 DEFAULT NULL
    ) RETURN NUMBER;

    -- ----------------------------------------------------------------
    -- RETO 04: OPERACIONES DE DML (PROCEDIMIENTOS)
    -- Disponen de dos parámetros de salida: P_CODIGO_ERROR y P_MENSAJE_ERROR
    -- ----------------------------------------------------------------

    -- Procedimiento para crear un comerciante
    PROCEDURE PR_CREAR_COMERCIANTE (
        P_NOMBRE_RAZON_SOCIAL IN  VARCHAR2,
        P_DEPARTAMENTO        IN  VARCHAR2,
        P_MUNICIPIO           IN  VARCHAR2,
        P_TELEFONO            IN  VARCHAR2 DEFAULT NULL,
        P_CORREO_ELECTRONICO  IN  VARCHAR2 DEFAULT NULL,
        P_FECHA_REGISTRO      IN  DATE,
        P_ESTADO              IN  VARCHAR2,
        P_ID_GENERADO         OUT NUMBER,
        P_CODIGO_ERROR        OUT NUMBER,
        P_MENSAJE_ERROR       OUT VARCHAR2
    );

    -- Procedimiento para actualizar un comerciante
    PROCEDURE PR_ACTUALIZAR_COMERCIANTE (
        P_ID_COMERCIANTE      IN  NUMBER,
        P_NOMBRE_RAZON_SOCIAL IN  VARCHAR2,
        P_DEPARTAMENTO        IN  VARCHAR2,
        P_MUNICIPIO           IN  VARCHAR2,
        P_TELEFONO            IN  VARCHAR2 DEFAULT NULL,
        P_CORREO_ELECTRONICO  IN  VARCHAR2 DEFAULT NULL,
        P_FECHA_REGISTRO      IN  DATE,
        P_ESTADO              IN  VARCHAR2,
        P_CODIGO_ERROR        OUT NUMBER,
        P_MENSAJE_ERROR       OUT VARCHAR2
    );

    -- Procedimiento para actualizar el estado de un comerciante (Activar/Inactivar / PATCH)
    PROCEDURE PR_CAMBIAR_ESTADO_COMERCIANTE (
        P_ID_COMERCIANTE      IN  NUMBER,
        P_NUEVO_ESTADO        IN  VARCHAR2,
        P_CODIGO_ERROR        OUT NUMBER,
        P_MENSAJE_ERROR       OUT VARCHAR2
    );

    -- Procedimiento para eliminar un comerciante
    PROCEDURE PR_ELIMINAR_COMERCIANTE (
        P_ID_COMERCIANTE      IN  NUMBER,
        P_CODIGO_ERROR        OUT NUMBER,
        P_MENSAJE_ERROR       OUT VARCHAR2
    );

    -- ----------------------------------------------------------------
    -- RETO 05: REPORTE DE COMERCIANTES REGISTRADOS Y ACTIVOS
    -- Retorna cursor por referencia ordenado desc por cant de establecimientos
    -- ----------------------------------------------------------------
    FUNCTION FN_REPORTE_COMERCIANTES_ACTIVOS
    RETURN SYS_REFCURSOR;

    -- ----------------------------------------------------------------
    -- FUNCIONES AUXILIARES DE VALIDACIÓN
    -- ----------------------------------------------------------------
    FUNCTION FN_VALIDAR_CORREO(P_CORREO IN VARCHAR2) RETURN BOOLEAN;
    FUNCTION FN_VALIDAR_TELEFONO(P_TELEFONO IN VARCHAR2) RETURN BOOLEAN;

END PKG_COMERCIANTES;
/
