-- ====================================================================
-- EVALUACIÓN PL/SQL FULLSTACK - RETO 04 Y RETO 05
-- SCRIPT 05: CUERPO DEL PAQUETE PKG_COMERCIANTES
-- Motor: Oracle 11gR2 o superior
-- REGLA ESTRICTA: Sin cursores implícitos (todos son explícitos)
-- ====================================================================

CREATE OR REPLACE PACKAGE BODY PKG_COMERCIANTES AS

    -- Constantes para códigos de error
    C_ERROR_CAMPOS_OBLIGATORIOS CONSTANT NUMBER := -20001;
    C_ERROR_FORMATO_CORREO      CONSTANT NUMBER := -20002;
    C_ERROR_ESTADO_INVALIDO     CONSTANT NUMBER := -20003;
    C_ERROR_NO_ENCONTRADO       CONSTANT NUMBER := -20004;
    C_ERROR_PARAMETROS          CONSTANT NUMBER := -20005;

    -- ================================================================
    -- FUNCIONES AUXILIARES DE VALIDACIÓN
    -- ================================================================
    
    FUNCTION FN_VALIDAR_CORREO(P_CORREO IN VARCHAR2) RETURN BOOLEAN IS
    BEGIN
        IF P_CORREO IS NULL OR TRIM(P_CORREO) IS NULL THEN
            RETURN TRUE;
        END IF;
        
        IF REGEXP_LIKE(TRIM(P_CORREO), '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$') THEN
            RETURN TRUE;
        ELSE
            RETURN FALSE;
        END IF;
    END FN_VALIDAR_CORREO;

    FUNCTION FN_VALIDAR_TELEFONO(P_TELEFONO IN VARCHAR2) RETURN BOOLEAN IS
    BEGIN
        IF P_TELEFONO IS NULL OR TRIM(P_TELEFONO) IS NULL THEN
            RETURN TRUE;
        END IF;
        
        -- Permite números, espacios, guiones y prefijo '+'
        IF REGEXP_LIKE(TRIM(P_TELEFONO), '^[+]?[0-9\s-]{7,20}$') THEN
            RETURN TRUE;
        ELSE
            RETURN FALSE;
        END IF;
    END FN_VALIDAR_TELEFONO;

    -- ================================================================
    -- RETO 04: CONSULTAR POR ID (FUNCIÓN)
    -- ================================================================
    FUNCTION FN_CONSULTAR_POR_ID (
        P_ID_COMERCIANTE IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                c.ID_COMERCIANTE,
                c.NOMBRE_RAZON_SOCIAL,
                c.DEPARTAMENTO,
                c.MUNICIPIO,
                c.TELEFONO,
                c.CORREO_ELECTRONICO,
                c.FECHA_REGISTRO,
                c.ESTADO,
                NVL(SUM(e.INGRESOS), 0) AS TOTAL_ACTIVOS,
                NVL(SUM(e.NUMERO_EMPLEADOS), 0) AS CANTIDAD_EMPLEADOS
            FROM COMERCIANTE c
            LEFT JOIN ESTABLECIMIENTO e ON c.ID_COMERCIANTE = e.ID_COMERCIANTE
            WHERE c.ID_COMERCIANTE = P_ID_COMERCIANTE
            GROUP BY 
                c.ID_COMERCIANTE,
                c.NOMBRE_RAZON_SOCIAL,
                c.DEPARTAMENTO,
                c.MUNICIPIO,
                c.TELEFONO,
                c.CORREO_ELECTRONICO,
                c.FECHA_REGISTRO,
                c.ESTADO;

        RETURN v_cursor;
    END FN_CONSULTAR_POR_ID;

    -- ================================================================
    -- RETO 04: CONSULTAR CON FILTROS Y PAGINACIÓN (FUNCIÓN)
    -- Compatible con Oracle 11gR2 (ROWNUM anidado)
    -- ================================================================
    FUNCTION FN_CONSULTAR_COMERCIANTES (
        P_NOMBRE_RAZON_SOCIAL IN VARCHAR2 DEFAULT NULL,
        P_MUNICIPIO           IN VARCHAR2 DEFAULT NULL,
        P_FECHA_REGISTRO      IN DATE     DEFAULT NULL,
        P_ESTADO              IN VARCHAR2 DEFAULT NULL,
        P_PAGINA              IN NUMBER   DEFAULT 1,
        P_REGISTROS_POR_PAG   IN NUMBER   DEFAULT 5
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
        v_pagina NUMBER := NVL(P_PAGINA, 1);
        v_limite NUMBER := NVL(P_REGISTROS_POR_PAG, 5);
        v_inicio NUMBER;
        v_fin    NUMBER;
    BEGIN
        IF v_pagina < 1 THEN
            v_pagina := 1;
        END IF;
        IF v_limite < 1 THEN
            v_limite := 5;
        END IF;

        v_inicio := ((v_pagina - 1) * v_limite) + 1;
        v_fin    := v_pagina * v_limite;

        OPEN v_cursor FOR
            SELECT * FROM (
                SELECT sub_res.*, ROWNUM AS RNUM FROM (
                    SELECT 
                        c.ID_COMERCIANTE,
                        c.NOMBRE_RAZON_SOCIAL,
                        c.DEPARTAMENTO,
                        c.MUNICIPIO,
                        c.TELEFONO,
                        c.CORREO_ELECTRONICO,
                        c.FECHA_REGISTRO,
                        c.ESTADO,
                        COUNT(e.ID_ESTABLECIMIENTO) AS CANTIDAD_ESTABLECIMIENTOS,
                        NVL(SUM(e.INGRESOS), 0) AS TOTAL_ACTIVOS,
                        NVL(SUM(e.NUMERO_EMPLEADOS), 0) AS CANTIDAD_EMPLEADOS
                    FROM COMERCIANTE c
                    LEFT JOIN ESTABLECIMIENTO e ON c.ID_COMERCIANTE = e.ID_COMERCIANTE
                    WHERE (P_NOMBRE_RAZON_SOCIAL IS NULL OR UPPER(c.NOMBRE_RAZON_SOCIAL) LIKE '%' || UPPER(TRIM(P_NOMBRE_RAZON_SOCIAL)) || '%')
                      AND (P_MUNICIPIO IS NULL OR UPPER(c.MUNICIPIO) = UPPER(TRIM(P_MUNICIPIO)))
                      AND (P_FECHA_REGISTRO IS NULL OR TRUNC(c.FECHA_REGISTRO) = TRUNC(P_FECHA_REGISTRO))
                      AND (P_ESTADO IS NULL OR UPPER(c.ESTADO) = UPPER(TRIM(P_ESTADO)))
                    GROUP BY 
                        c.ID_COMERCIANTE,
                        c.NOMBRE_RAZON_SOCIAL,
                        c.DEPARTAMENTO,
                        c.MUNICIPIO,
                        c.TELEFONO,
                        c.CORREO_ELECTRONICO,
                        c.FECHA_REGISTRO,
                        c.ESTADO
                    ORDER BY c.ID_COMERCIANTE DESC
                ) sub_res
                WHERE ROWNUM <= v_fin
            )
            WHERE RNUM >= v_inicio;

        RETURN v_cursor;
    END FN_CONSULTAR_COMERCIANTES;

    -- ================================================================
    -- FUNCIÓN AUXILIAR: CONTAR TOTAL DE REGISTROS (Cursor Explícito)
    -- ================================================================
    FUNCTION FN_CONTAR_COMERCIANTES (
        P_NOMBRE_RAZON_SOCIAL IN VARCHAR2 DEFAULT NULL,
        P_MUNICIPIO           IN VARCHAR2 DEFAULT NULL,
        P_FECHA_REGISTRO      IN DATE     DEFAULT NULL,
        P_ESTADO              IN VARCHAR2 DEFAULT NULL
    ) RETURN NUMBER IS
        v_total NUMBER := 0;
        
        -- Cursor explícito para evitar cursores implícitos
        CURSOR c_contar IS
            SELECT COUNT(1)
            FROM COMERCIANTE c
            WHERE (P_NOMBRE_RAZON_SOCIAL IS NULL OR UPPER(c.NOMBRE_RAZON_SOCIAL) LIKE '%' || UPPER(TRIM(P_NOMBRE_RAZON_SOCIAL)) || '%')
              AND (P_MUNICIPIO IS NULL OR UPPER(c.MUNICIPIO) = UPPER(TRIM(P_MUNICIPIO)))
              AND (P_FECHA_REGISTRO IS NULL OR TRUNC(c.FECHA_REGISTRO) = TRUNC(P_FECHA_REGISTRO))
              AND (P_ESTADO IS NULL OR UPPER(c.ESTADO) = UPPER(TRIM(P_ESTADO)));
    BEGIN
        OPEN c_contar;
        FETCH c_contar INTO v_total;
        CLOSE c_contar;

        RETURN v_total;
    END FN_CONTAR_COMERCIANTES;

    -- ================================================================
    -- RETO 04: CREAR COMERCIANTE (PROCEDIMIENTO)
    -- ================================================================
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
    ) IS
        v_id_nuevo NUMBER;
        
        -- Cursor explícito para obtener el siguiente valor de secuencia
        CURSOR c_secuencia IS
            SELECT SEQ_COMERCIANTE.NEXTVAL FROM DUAL;
    BEGIN
        P_ID_GENERADO := NULL;

        -- 1. Validaciones de obligatoriedad
        IF P_NOMBRE_RAZON_SOCIAL IS NULL OR TRIM(P_NOMBRE_RAZON_SOCIAL) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Nombre o Razón Social es obligatorio.';
            RETURN;
        END IF;

        IF P_DEPARTAMENTO IS NULL OR TRIM(P_DEPARTAMENTO) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Departamento es obligatorio.';
            RETURN;
        END IF;

        IF P_MUNICIPIO IS NULL OR TRIM(P_MUNICIPIO) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Municipio es obligatorio.';
            RETURN;
        END IF;

        IF P_FECHA_REGISTRO IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'La Fecha de Registro es obligatoria.';
            RETURN;
        END IF;

        IF P_ESTADO IS NULL OR TRIM(P_ESTADO) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Estado es obligatorio.';
            RETURN;
        END IF;

        -- 2. Validación de Estado válido
        IF TRIM(P_ESTADO) NOT IN ('Registrado', 'Activo', 'Inactivo', 'Suspendido', 'Cancelado') THEN
            P_CODIGO_ERROR := C_ERROR_ESTADO_INVALIDO;
            P_MENSAJE_ERROR := 'Estado inválido. Debe ser: Registrado, Activo, Inactivo, Suspendido o Cancelado.';
            RETURN;
        END IF;

        -- 3. Validación de estructura de correo electrónico (si viene informado)
        IF P_CORREO_ELECTRONICO IS NOT NULL AND NOT FN_VALIDAR_CORREO(P_CORREO_ELECTRONICO) THEN
            P_CODIGO_ERROR := C_ERROR_FORMATO_CORREO;
            P_MENSAJE_ERROR := 'El formato del correo electrónico es inválido.';
            RETURN;
        END IF;

        -- 4. Validación de formato de teléfono (si viene informado)
        IF P_TELEFONO IS NOT NULL AND NOT FN_VALIDAR_TELEFONO(P_TELEFONO) THEN
            P_CODIGO_ERROR := C_ERROR_PARAMETROS;
            P_MENSAJE_ERROR := 'El formato del teléfono es inválido.';
            RETURN;
        END IF;

        -- Obtención de secuencia mediante cursor explícito
        OPEN c_secuencia;
        FETCH c_secuencia INTO v_id_nuevo;
        CLOSE c_secuencia;

        -- Inserción
        INSERT INTO COMERCIANTE (
            ID_COMERCIANTE,
            NOMBRE_RAZON_SOCIAL,
            DEPARTAMENTO,
            MUNICIPIO,
            TELEFONO,
            CORREO_ELECTRONICO,
            FECHA_REGISTRO,
            ESTADO,
            FECHA_ACTUALIZACION,
            USUARIO_ACTUALIZACION
        ) VALUES (
            v_id_nuevo,
            TRIM(P_NOMBRE_RAZON_SOCIAL),
            TRIM(P_DEPARTAMENTO),
            TRIM(P_MUNICIPIO),
            TRIM(P_TELEFONO),
            TRIM(P_CORREO_ELECTRONICO),
            P_FECHA_REGISTRO,
            TRIM(P_ESTADO),
            SYSDATE,
            NVL(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER)
        );

        P_ID_GENERADO := v_id_nuevo;
        P_CODIGO_ERROR := 0;
        P_MENSAJE_ERROR := NULL;

    EXCEPTION
        WHEN OTHERS THEN
            IF c_secuencia%ISOPEN THEN
                CLOSE c_secuencia;
            END IF;
            P_ID_GENERADO   := NULL;
            P_CODIGO_ERROR  := SQLCODE;
            P_MENSAJE_ERROR := SQLERRM;
    END PR_CREAR_COMERCIANTE;

    -- ================================================================
    -- RETO 04: ACTUALIZAR COMERCIANTE (PROCEDIMIENTO)
    -- ================================================================
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
    ) IS
        v_existe NUMBER := 0;
        
        -- Cursor explícito para validar existencia
        CURSOR c_existe IS
            SELECT 1 FROM COMERCIANTE WHERE ID_COMERCIANTE = P_ID_COMERCIANTE;
    BEGIN
        -- Validar ID
        IF P_ID_COMERCIANTE IS NULL OR P_ID_COMERCIANTE <= 0 THEN
            P_CODIGO_ERROR := C_ERROR_PARAMETROS;
            P_MENSAJE_ERROR := 'El identificador del comerciante es obligatorio y debe ser válido.';
            RETURN;
        END IF;

        -- Validar existencia con cursor explícito
        OPEN c_existe;
        FETCH c_existe INTO v_existe;
        IF c_existe%NOTFOUND THEN
            CLOSE c_existe;
            P_CODIGO_ERROR := C_ERROR_NO_ENCONTRADO;
            P_MENSAJE_ERROR := 'No se encontró el comerciante con ID ' || P_ID_COMERCIANTE;
            RETURN;
        END IF;
        CLOSE c_existe;

        -- Validaciones de obligatoriedad
        IF P_NOMBRE_RAZON_SOCIAL IS NULL OR TRIM(P_NOMBRE_RAZON_SOCIAL) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Nombre o Razón Social es obligatorio.';
            RETURN;
        END IF;

        IF P_DEPARTAMENTO IS NULL OR TRIM(P_DEPARTAMENTO) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Departamento es obligatorio.';
            RETURN;
        END IF;

        IF P_MUNICIPIO IS NULL OR TRIM(P_MUNICIPIO) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Municipio es obligatorio.';
            RETURN;
        END IF;

        IF P_FECHA_REGISTRO IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'La Fecha de Registro es obligatoria.';
            RETURN;
        END IF;

        IF P_ESTADO IS NULL OR TRIM(P_ESTADO) IS NULL THEN
            P_CODIGO_ERROR := C_ERROR_CAMPOS_OBLIGATORIOS;
            P_MENSAJE_ERROR := 'El Estado es obligatorio.';
            RETURN;
        END IF;

        IF TRIM(P_ESTADO) NOT IN ('Registrado', 'Activo', 'Inactivo', 'Suspendido', 'Cancelado') THEN
            P_CODIGO_ERROR := C_ERROR_ESTADO_INVALIDO;
            P_MENSAJE_ERROR := 'Estado inválido. Debe ser: Registrado, Activo, Inactivo, Suspendido o Cancelado.';
            RETURN;
        END IF;

        IF P_CORREO_ELECTRONICO IS NOT NULL AND NOT FN_VALIDAR_CORREO(P_CORREO_ELECTRONICO) THEN
            P_CODIGO_ERROR := C_ERROR_FORMATO_CORREO;
            P_MENSAJE_ERROR := 'El formato del correo electrónico es inválido.';
            RETURN;
        END IF;

        IF P_TELEFONO IS NOT NULL AND NOT FN_VALIDAR_TELEFONO(P_TELEFONO) THEN
            P_CODIGO_ERROR := C_ERROR_PARAMETROS;
            P_MENSAJE_ERROR := 'El formato del teléfono es inválido.';
            RETURN;
        END IF;

        -- Actualización
        UPDATE COMERCIANTE
        SET NOMBRE_RAZON_SOCIAL   = TRIM(P_NOMBRE_RAZON_SOCIAL),
            DEPARTAMENTO          = TRIM(P_DEPARTAMENTO),
            MUNICIPIO             = TRIM(P_MUNICIPIO),
            TELEFONO              = TRIM(P_TELEFONO),
            CORREO_ELECTRONICO    = TRIM(P_CORREO_ELECTRONICO),
            FECHA_REGISTRO        = P_FECHA_REGISTRO,
            ESTADO                = TRIM(P_ESTADO),
            FECHA_ACTUALIZACION   = SYSDATE,
            USUARIO_ACTUALIZACION = NVL(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER)
        WHERE ID_COMERCIANTE = P_ID_COMERCIANTE;

        P_CODIGO_ERROR := 0;
        P_MENSAJE_ERROR := NULL;

    EXCEPTION
        WHEN OTHERS THEN
            IF c_existe%ISOPEN THEN
                CLOSE c_existe;
            END IF;
            P_CODIGO_ERROR  := SQLCODE;
            P_MENSAJE_ERROR := SQLERRM;
    END PR_ACTUALIZAR_COMERCIANTE;

    -- ================================================================
    -- RETO 08: CAMBIAR ESTADO COMERCIANTE (PATCH)
    -- ================================================================
    PROCEDURE PR_CAMBIAR_ESTADO_COMERCIANTE (
        P_ID_COMERCIANTE      IN  NUMBER,
        P_NUEVO_ESTADO        IN  VARCHAR2,
        P_CODIGO_ERROR        OUT NUMBER,
        P_MENSAJE_ERROR       OUT VARCHAR2
    ) IS
        v_existe NUMBER := 0;
        CURSOR c_existe IS
            SELECT 1 FROM COMERCIANTE WHERE ID_COMERCIANTE = P_ID_COMERCIANTE;
    BEGIN
        IF P_ID_COMERCIANTE IS NULL OR P_ID_COMERCIANTE <= 0 THEN
            P_CODIGO_ERROR := C_ERROR_PARAMETROS;
            P_MENSAJE_ERROR := 'El ID del comerciante es obligatorio.';
            RETURN;
        END IF;

        IF P_NUEVO_ESTADO IS NULL OR TRIM(P_NUEVO_ESTADO) NOT IN ('Registrado', 'Activo', 'Inactivo', 'Suspendido', 'Cancelado') THEN
            P_CODIGO_ERROR := C_ERROR_ESTADO_INVALIDO;
            P_MENSAJE_ERROR := 'El estado especificado no es válido.';
            RETURN;
        END IF;

        OPEN c_existe;
        FETCH c_existe INTO v_existe;
        IF c_existe%NOTFOUND THEN
            CLOSE c_existe;
            P_CODIGO_ERROR := C_ERROR_NO_ENCONTRADO;
            P_MENSAJE_ERROR := 'No se encontró el comerciante.';
            RETURN;
        END IF;
        CLOSE c_existe;

        UPDATE COMERCIANTE
        SET ESTADO = TRIM(P_NUEVO_ESTADO),
            FECHA_ACTUALIZACION = SYSDATE,
            USUARIO_ACTUALIZACION = NVL(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER)
        WHERE ID_COMERCIANTE = P_ID_COMERCIANTE;

        P_CODIGO_ERROR := 0;
        P_MENSAJE_ERROR := NULL;

    EXCEPTION
        WHEN OTHERS THEN
            IF c_existe%ISOPEN THEN
                CLOSE c_existe;
            END IF;
            P_CODIGO_ERROR := SQLCODE;
            P_MENSAJE_ERROR := SQLERRM;
    END PR_CAMBIAR_ESTADO_COMERCIANTE;

    -- ================================================================
    -- RETO 04: ELIMINAR COMERCIANTE (PROCEDIMIENTO)
    -- ================================================================
    PROCEDURE PR_ELIMINAR_COMERCIANTE (
        P_ID_COMERCIANTE      IN  NUMBER,
        P_CODIGO_ERROR        OUT NUMBER,
        P_MENSAJE_ERROR       OUT VARCHAR2
    ) IS
        v_existe NUMBER := 0;
        CURSOR c_existe IS
            SELECT 1 FROM COMERCIANTE WHERE ID_COMERCIANTE = P_ID_COMERCIANTE;
    BEGIN
        IF P_ID_COMERCIANTE IS NULL OR P_ID_COMERCIANTE <= 0 THEN
            P_CODIGO_ERROR := C_ERROR_PARAMETROS;
            P_MENSAJE_ERROR := 'Identificador de comerciante inválido.';
            RETURN;
        END IF;

        OPEN c_existe;
        FETCH c_existe INTO v_existe;
        IF c_existe%NOTFOUND THEN
            CLOSE c_existe;
            P_CODIGO_ERROR := C_ERROR_NO_ENCONTRADO;
            P_MENSAJE_ERROR := 'No se encontró el comerciante a eliminar.';
            RETURN;
        END IF;
        CLOSE c_existe;

        -- Eliminación en cascada de establecimientos (por FK ON DELETE CASCADE o explícito)
        DELETE FROM ESTABLECIMIENTO WHERE ID_COMERCIANTE = P_ID_COMERCIANTE;
        DELETE FROM COMERCIANTE WHERE ID_COMERCIANTE = P_ID_COMERCIANTE;

        P_CODIGO_ERROR := 0;
        P_MENSAJE_ERROR := NULL;

    EXCEPTION
        WHEN OTHERS THEN
            IF c_existe%ISOPEN THEN
                CLOSE c_existe;
            END IF;
            P_CODIGO_ERROR  := SQLCODE;
            P_MENSAJE_ERROR := SQLERRM;
    END PR_ELIMINAR_COMERCIANTE;

    -- ================================================================
    -- RETO 05: REPORTE DE COMERCIANTES REGISTRADOS Y ACTIVOS
    -- Cursor referenciado ordenado DESC por cantidad de establecimientos
    -- ================================================================
    FUNCTION FN_REPORTE_COMERCIANTES_ACTIVOS
    RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                c.ID_COMERCIANTE,
                c.NOMBRE_RAZON_SOCIAL,
                c.DEPARTAMENTO,
                c.MUNICIPIO,
                c.TELEFONO,
                c.CORREO_ELECTRONICO,
                c.FECHA_REGISTRO,
                c.ESTADO,
                COUNT(e.ID_ESTABLECIMIENTO) AS CANTIDAD_ESTABLECIMIENTOS,
                NVL(SUM(e.INGRESOS), 0) AS TOTAL_ACTIVOS,
                NVL(SUM(e.NUMERO_EMPLEADOS), 0) AS CANTIDAD_EMPLEADOS
            FROM COMERCIANTE c
            LEFT JOIN ESTABLECIMIENTO e ON c.ID_COMERCIANTE = e.ID_COMERCIANTE
            WHERE c.ESTADO IN ('Registrado', 'Activo')
            GROUP BY 
                c.ID_COMERCIANTE,
                c.NOMBRE_RAZON_SOCIAL,
                c.DEPARTAMENTO,
                c.MUNICIPIO,
                c.TELEFONO,
                c.CORREO_ELECTRONICO,
                c.FECHA_REGISTRO,
                c.ESTADO
            ORDER BY COUNT(e.ID_ESTABLECIMIENTO) DESC, c.NOMBRE_RAZON_SOCIAL ASC;

        RETURN v_cursor;
    END FN_REPORTE_COMERCIANTES_ACTIVOS;

END PKG_COMERCIANTES;
/
