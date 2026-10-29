-- ====================================================================
-- EVALUACIÓN PL/SQL FULLSTACK - SCRIPT MAESTRO DE EJECUCIÓN
-- Orden de ejecución para ambiente de calificación
-- ====================================================================

PROMPT --------------------------------------------------------------------
PROMPT 1. Creando tablas, llaves primarias, foraneas e indices...
PROMPT --------------------------------------------------------------------
@@01_create_tables_and_indexes.sql;

PROMPT --------------------------------------------------------------------
PROMPT 2. Creando secuencias y disparadores (triggers de ID y Auditoria)...
PROMPT --------------------------------------------------------------------
@@02_sequences_and_triggers.sql;

PROMPT --------------------------------------------------------------------
PROMPT 3. Insertando datos semilla (Comerciantes, Establecimientos, Deptos)..
PROMPT --------------------------------------------------------------------
@@03_seed_data.sql;

PROMPT --------------------------------------------------------------------
PROMPT 4. Creando especificacion del paquete PKG_COMERCIANTES...
PROMPT --------------------------------------------------------------------
@@04_pkg_comerciantes_spec.sql;

PROMPT --------------------------------------------------------------------
PROMPT 5. Creando cuerpo del paquete PKG_COMERCIANTES...
PROMPT --------------------------------------------------------------------
@@05_pkg_comerciantes_body.sql;

PROMPT --------------------------------------------------------------------
PROMPT Base de datos configurada exitosamente!
PROMPT --------------------------------------------------------------------
COMMIT;
