import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { Footer } from '../components/Footer';
import { useToast } from '../context/ToastContext';
import { comercianteService } from '../services/comercianteService';
import { catalogoService } from '../services/catalogoService';
import { establecimientoService } from '../services/establecimientoService';
import type { Departamento, Municipio, Establecimiento, EstadoComerciante } from '../types';
import {
  Building2,
  Plus,
  Trash2,
  Edit3,
  DollarSign,
  Users,
  CheckCircle,
  AlertTriangle,
  ArrowLeft,
  X,
} from 'lucide-react';

export const FormularioPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const idEdicion = searchParams.get('id');
  const esEdicion = !!idEdicion;

  const navigate = useNavigate();
  const { showToast } = useToast();

  // Estados de catálogo (listas de valores)
  const [departamentos, setDepartamentos] = useState<Departamento[]>([]);
  const [municipios, setMunicipios] = useState<Municipio[]>([]);
  const [cargandoCatalogos, setCargandoCatalogos] = useState(false);

  // Estados del Formulario de Comerciante
  const [nombreRazonSocial, setNombreRazonSocial] = useState('');
  const [departamentoSeleccionado, setDepartamentoSeleccionado] = useState('');
  const [municipioSeleccionado, setMunicipioSeleccionado] = useState('');
  const [telefono, setTelefono] = useState('');
  const [correoElectronico, setCorreoElectronico] = useState('');
  const [fechaRegistro, setFechaRegistro] = useState(new Date().toISOString().split('T')[0]);
  const [estado, setEstado] = useState<EstadoComerciante>('Activo');
  const [poseeEstablecimientos, setPoseeEstablecimientos] = useState(false);

  // Lista de Establecimientos
  const [establecimientos, setEstablecimientos] = useState<Establecimiento[]>([]);

  // Modal / Formulario para Crear / Editar Establecimiento
  const [modalEstablecimientoAbierto, setModalEstablecimientoAbierto] = useState(false);
  const [estIndiceEdicion, setEstIndiceEdicion] = useState<number | null>(null);
  const [estNombre, setEstNombre] = useState('');
  const [estIngresos, setEstIngresos] = useState('');
  const [estEmpleados, setEstEmpleados] = useState('');
  const [estErrores, setEstErrores] = useState<Record<string, string>>({});

  // Errores de validación en campos principales
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [guardando, setGuardando] = useState(false);
  const [cargandoDatos, setCargandoDatos] = useState(false);

  // 1. Cargar Departamentos al montar la página
  useEffect(() => {
    let isMounted = true;
    setCargandoCatalogos(true);
    catalogoService.getDepartamentos()
      .then((deptos) => {
        if (isMounted) setDepartamentos(deptos);
      })
      .catch((err: any) => {
        if (isMounted) {
          showToast('Error al cargar lista de departamentos: ' + err.message, 'error');
        }
      })
      .finally(() => {
        if (isMounted) setCargandoCatalogos(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  // 2. Cargar Municipios en cascada al cambiar Departamento
  useEffect(() => {
    if (!departamentoSeleccionado) {
      setMunicipios([]);
      return;
    }

    // Si aún se están cargando los departamentos de referencia, esperamos a que estén listos
    if (cargandoCatalogos && departamentos.length === 0) {
      return;
    }

    let isMounted = true;
    const deptoEncontrado = departamentos.find(
      (d) => d.nombre.trim().toLowerCase() === departamentoSeleccionado.trim().toLowerCase()
    );

    const promesa = deptoEncontrado?.id
      ? catalogoService.getMunicipiosPorDepartamento(deptoEncontrado.id)
      : catalogoService.getMunicipiosPorNombre(departamentoSeleccionado);

    promesa
      .then((m) => {
        if (isMounted) setMunicipios(m);
      })
      .catch((err: any) => {
        if (isMounted) {
          const msg = err.response?.data?.mensaje || err.message;
          showToast(`Error al cargar municipios para ${departamentoSeleccionado}: ${msg}`, 'error');
        }
      });

    return () => {
      isMounted = false;
    };
  }, [departamentoSeleccionado, departamentos, cargandoCatalogos]);

  // 3. Si es edición, cargar datos del comerciante existente
  useEffect(() => {
    if (!esEdicion || !idEdicion) return;

    let isMounted = true;
    const cargarComerciante = async () => {
      setCargandoDatos(true);
      try {
        const c = await comercianteService.getById(Number(idEdicion));
        if (!isMounted) return;
        setNombreRazonSocial(c.nombreRazonSocial);
        setDepartamentoSeleccionado(c.departamento);
        setMunicipioSeleccionado(c.municipio);
        setTelefono(c.telefono || '');
        setCorreoElectronico(c.correoElectronico || '');
        setFechaRegistro(c.fechaRegistro);
        setEstado(c.estado);

        const ests = c.establecimientos || [];
        setEstablecimientos(ests);
        setPoseeEstablecimientos(ests.length > 0);
      } catch (err: any) {
        if (!isMounted) return;
        showToast('Error al cargar datos del comerciante: ' + (err.response?.data?.mensaje || err.message), 'error');
        navigate('/');
      } finally {
        if (isMounted) setCargandoDatos(false);
      }
    };

    cargarComerciante();

    return () => {
      isMounted = false;
    };
  }, [esEdicion, idEdicion]);

  // Cálculo en tiempo real de totales para el footer (Reto 14)
  const totalesCalculados = useMemo(() => {
    const totalIngresos = establecimientos.reduce((acc, curr) => acc + (Number(curr.ingresos) || 0), 0);
    const totalEmpleados = establecimientos.reduce((acc, curr) => acc + (Number(curr.numeroEmpleados) || 0), 0);

    const formatter = new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 2,
    });

    return {
      ingresosTexto: formatter.format(totalIngresos),
      cantidadEmpleados: totalEmpleados,
    };
  }, [establecimientos]);

  // Validaciones de datos generales (Reto 14 / OWASP)
  const validarFormularioPrincipal = (): boolean => {
    const nuevosErrores: Record<string, string> = {};

    if (!nombreRazonSocial.trim()) {
      nuevosErrores.nombreRazonSocial = 'La Razón Social o Nombre es obligatorio';
    }

    if (!departamentoSeleccionado.trim()) {
      nuevosErrores.departamento = 'Debe seleccionar un Departamento';
    }

    if (!municipioSeleccionado.trim()) {
      nuevosErrores.municipio = 'Debe seleccionar una Ciudad o Municipio';
    }

    if (!fechaRegistro) {
      nuevosErrores.fechaRegistro = 'La Fecha de Registro es obligatoria';
    }

    if (telefono.trim() && !/^[+]?[0-9\s-]{7,20}$/.test(telefono.trim())) {
      nuevosErrores.telefono = 'Formato de teléfono inválido (solo dígitos, espacios, guión o +)';
    }

    if (correoElectronico.trim() && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(correoElectronico.trim())) {
      nuevosErrores.correoElectronico = 'Formato de correo electrónico inválido';
    }

    setErrores(nuevosErrores);
    return Object.keys(nuevosErrores).length === 0;
  };

  // Abrir modal de nuevo establecimiento
  const abrirModalNuevoEstablecimiento = () => {
    setEstIndiceEdicion(null);
    setEstNombre('');
    setEstIngresos('');
    setEstEmpleados('');
    setEstErrores({});
    setModalEstablecimientoAbierto(true);
  };

  // Abrir modal para editar establecimiento
  const abrirModalEditarEstablecimiento = (index: number) => {
    const est = establecimientos[index];
    setEstIndiceEdicion(index);
    setEstNombre(est.nombreEstablecimiento);
    setEstIngresos(String(est.ingresos));
    setEstEmpleados(String(est.numeroEmpleados));
    setEstErrores({});
    setModalEstablecimientoAbierto(true);
  };

  // Guardar establecimiento en la lista local (o en BD si es edición directa)
  const handleGuardarEstablecimientoModal = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs: Record<string, string> = {};

    if (!estNombre.trim()) {
      errs.nombre = 'El nombre del establecimiento es obligatorio';
    }

    const ingresosNum = parseFloat(estIngresos);
    if (isNaN(ingresosNum) || ingresosNum < 0) {
      errs.ingresos = 'Los ingresos deben ser un número positivo (permite 2 decimales)';
    }

    const empleadosNum = parseInt(estEmpleados, 10);
    if (isNaN(empleadosNum) || empleadosNum < 0) {
      errs.empleados = 'El número de empleados debe ser un número entero mayor o igual a 0';
    }

    if (Object.keys(errs).length > 0) {
      setEstErrores(errs);
      return;
    }

    const item: Establecimiento = {
      nombreEstablecimiento: estNombre.trim(),
      ingresos: ingresosNum,
      numeroEmpleados: empleadosNum,
    };

    if (esEdicion && idEdicion) {
      // Si estamos editando un comerciante ya persistido, actualizamos directamente en BD si ya tiene ID
      try {
        if (estIndiceEdicion !== null && establecimientos[estIndiceEdicion].id) {
          const idEst = establecimientos[estIndiceEdicion].id!;
          await establecimientoService.update(idEst, item);
          showToast('Establecimiento actualizado en base de datos', 'success');
        } else if (estIndiceEdicion === null) {
          item.comercianteId = Number(idEdicion);
          const creado = await establecimientoService.create(item);
          item.id = creado.id;
          showToast('Establecimiento registrado en base de datos', 'success');
        }
      } catch (err: any) {
        showToast('Error al persistir establecimiento: ' + (err.response?.data?.mensaje || err.message), 'error');
        return;
      }
    }

    if (estIndiceEdicion !== null) {
      // Actualizar en lista
      const nuevaLista = [...establecimientos];
      nuevaLista[estIndiceEdicion] = { ...nuevaLista[estIndiceEdicion], ...item };
      setEstablecimientos(nuevaLista);
    } else {
      // Agregar nuevo a lista
      setEstablecimientos((prev) => [...prev, item]);
    }

    setModalEstablecimientoAbierto(false);
  };

  const handleEliminarEstablecimiento = async (index: number) => {
    const est = establecimientos[index];
    if (est.id) {
      try {
        await establecimientoService.delete(est.id);
        showToast('Establecimiento eliminado de base de datos', 'success');
      } catch (err: any) {
        showToast('Error al eliminar establecimiento: ' + err.message, 'error');
        return;
      }
    }
    setEstablecimientos((prev) => prev.filter((_, i) => i !== index));
  };

  // Enviar Formulario General (Crear o Actualizar Comerciante)
  const handleSubmitGeneral = async () => {
    if (!validarFormularioPrincipal()) {
      showToast('Por favor complete los campos obligatorios con la información requerida.', 'error');
      return;
    }

    setGuardando(true);
    try {
      if (esEdicion && idEdicion) {
        await comercianteService.update(Number(idEdicion), {
          nombreRazonSocial,
          departamento: departamentoSeleccionado,
          municipio: municipioSeleccionado,
          telefono: telefono || undefined,
          correoElectronico: correoElectronico || undefined,
          fechaRegistro,
          estado,
        });
        showToast('¡Formulario de comerciante actualizado exitosamente!', 'success');
      } else {
        await comercianteService.create({
          nombreRazonSocial,
          departamento: departamentoSeleccionado,
          municipio: municipioSeleccionado,
          telefono: telefono || undefined,
          correoElectronico: correoElectronico || undefined,
          fechaRegistro,
          estado,
          establecimientos: poseeEstablecimientos ? establecimientos : [],
        });
        showToast('¡Nuevo comerciante y establecimientos registrados con éxito!', 'success');
      }

      navigate('/');
    } catch (err: any) {
      const msg = err.response?.data?.mensaje || 'Error al procesar la solicitud.';
      showToast(msg, 'error');
    } finally {
      setGuardando(false);
    }
  };

  if (cargandoDatos) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
        <Navbar />
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <p style={{ color: '#64748b' }}>Cargando datos del comerciante...</p>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', paddingBottom: '90px' }}>
      <Navbar />

      <main style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1.5rem', width: '100%' }}>
        {/* Cabecera del formulario */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <button
              onClick={() => navigate('/')}
              style={{
                background: 'transparent',
                display: 'flex',
                alignItems: 'center',
                color: '#64748b',
                padding: '0.4rem',
                borderRadius: '6px',
                border: '1px solid #cbd5e1',
              }}
            >
              <ArrowLeft size={18} />
            </button>
            <h1 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#1e293b' }}>
              {esEdicion ? nombreRazonSocial || 'Editar Formulario' : 'Crear Formulario de Comerciante'}
            </h1>
          </div>
        </div>

        {/* Sección: Datos Generales (Fiel al prototipo de la Página 12) */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          border: '1px solid #e2e8f0',
          padding: '1.75rem',
          boxShadow: '0 1px 3px rgba(0,0,0,0.04)',
          marginBottom: '2rem',
        }}>
          <h2 style={{ fontSize: '1rem', fontWeight: 700, color: '#1e293b', marginBottom: '1.25rem' }}>
            Datos Generales
          </h2>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '1.25rem',
          }}>
            {/* Razón Social */}
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Razón Social *
              </label>
              <input
                type="text"
                placeholder="Ej: Inversiones Los Andes S.A.S."
                value={nombreRazonSocial}
                onChange={(e) => setNombreRazonSocial(e.target.value)}
                className={errores.nombreRazonSocial ? 'input-error' : ''}
              />
              {errores.nombreRazonSocial && <span className="error-text">{errores.nombreRazonSocial}</span>}
            </div>

            {/* Correo Electrónico */}
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Correo Electrónico
              </label>
              <input
                type="email"
                placeholder="ejemplo@comercio.co"
                value={correoElectronico}
                onChange={(e) => setCorreoElectronico(e.target.value)}
                className={errores.correoElectronico ? 'input-error' : ''}
              />
              {errores.correoElectronico && <span className="error-text">{errores.correoElectronico}</span>}
            </div>

            {/* Departamento (Selector de Lista de Valores) */}
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Departamento *
              </label>
              <select
                value={departamentoSeleccionado}
                onChange={(e) => {
                  setDepartamentoSeleccionado(e.target.value);
                  setMunicipioSeleccionado('');
                }}
                className={errores.departamento ? 'input-error' : ''}
              >
                <option value="">Seleccione Departamento</option>
                {departamentos.map((d) => (
                  <option key={d.id} value={d.nombre}>{d.nombre}</option>
                ))}
              </select>
              {errores.departamento && <span className="error-text">{errores.departamento}</span>}
            </div>

            {/* Fecha de Registro */}
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Fecha de registro *
              </label>
              <input
                type="date"
                value={fechaRegistro}
                onChange={(e) => setFechaRegistro(e.target.value)}
                className={errores.fechaRegistro ? 'input-error' : ''}
              />
              {errores.fechaRegistro && <span className="error-text">{errores.fechaRegistro}</span>}
            </div>

            {/* Ciudad / Municipio (Filtrado en cascada) */}
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Ciudad o Municipio *
              </label>
              <select
                value={municipioSeleccionado}
                onChange={(e) => setMunicipioSeleccionado(e.target.value)}
                disabled={!departamentoSeleccionado}
                className={errores.municipio ? 'input-error' : ''}
              >
                <option value="">
                  {departamentoSeleccionado ? 'Seleccione Municipio' : 'Primero elija un departamento'}
                </option>
                {municipios.map((m) => (
                  <option key={m.id} value={m.nombre}>{m.nombre}</option>
                ))}
              </select>
              {errores.municipio && <span className="error-text">{errores.municipio}</span>}
            </div>

            {/* Teléfono */}
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Teléfono
              </label>
              <input
                type="text"
                placeholder="+57 300 123 4567"
                value={telefono}
                onChange={(e) => setTelefono(e.target.value)}
                className={errores.telefono ? 'input-error' : ''}
              />
              {errores.telefono && <span className="error-text">{errores.telefono}</span>}
            </div>

            {/* Estado */}
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Estado *
              </label>
              <select value={estado} onChange={(e) => setEstado(e.target.value as EstadoComerciante)}>
                <option value="Activo">Activo</option>
                <option value="Registrado">Registrado</option>
                <option value="Inactivo">Inactivo</option>
                <option value="Suspendido">Suspendido</option>
                <option value="Cancelado">Cancelado</option>
              </select>
            </div>

            {/* CheckBox ¿Posee establecimientos? (Página 11 y 12 del PDF) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem', paddingTop: '1.25rem' }}>
              <input
                type="checkbox"
                id="poseeEstablecimientos"
                checked={poseeEstablecimientos}
                onChange={(e) => setPoseeEstablecimientos(e.target.checked)}
                style={{ width: '18px', height: '18px', cursor: 'pointer' }}
              />
              <label
                htmlFor="poseeEstablecimientos"
                style={{ fontSize: '0.875rem', fontWeight: 600, color: '#1e293b', cursor: 'pointer' }}
              >
                ¿Posee establecimientos?
              </label>
            </div>
          </div>
        </div>

        {/* Sección Dinámica: Gestionar Establecimiento (Visible solo si el checkbox está activo) */}
        {poseeEstablecimientos && (
          <div style={{
            backgroundColor: '#ffffff',
            borderRadius: '8px',
            border: '1px solid #e2e8f0',
            padding: '1.75rem',
            boxShadow: '0 1px 3px rgba(0,0,0,0.04)',
            marginBottom: '2rem',
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: '1.5rem',
              borderBottom: '1px solid #f1f5f9',
              paddingBottom: '1rem',
            }}>
              <div>
                <h2 style={{ fontSize: '1rem', fontWeight: 700, color: '#1e293b' }}>
                  Gestionar Establecimiento
                </h2>
                <p style={{ fontSize: '0.8rem', color: '#64748b' }}>
                  Locales y puntos de atención vinculados a este comerciante
                </p>
              </div>

              <button
                type="button"
                onClick={abrirModalNuevoEstablecimiento}
                className="btn-primary"
                style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8125rem' }}
              >
                <Plus size={16} />
                Crear Establecimiento
              </button>
            </div>

            {/* Lista o Cuadrícula de Tarjetas de Establecimientos (Página 12 del PDF) */}
            {establecimientos.length === 0 ? (
              <div style={{
                textAlign: 'center',
                padding: '2.5rem',
                backgroundColor: '#f8fafc',
                borderRadius: '8px',
                border: '1px dashed #cbd5e1',
                color: '#64748b',
              }}>
                <Building2 size={36} style={{ margin: '0 auto 0.75rem', color: '#94a3b8' }} />
                <p style={{ fontWeight: 600 }}>No hay establecimientos registrados aún.</p>
                <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>
                  Haga clic en el botón superior "Crear Establecimiento" para añadir uno.
                </p>
              </div>
            ) : (
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
                gap: '1.25rem',
              }}>
                {establecimientos.map((est, idx) => (
                  <div
                    key={idx}
                    style={{
                      backgroundColor: '#ffffff',
                      border: '1px solid #e2e8f0',
                      borderRadius: '8px',
                      padding: '1.25rem',
                      boxShadow: '0 2px 4px rgba(0,0,0,0.03)',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'space-between',
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                        <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#0284c7', textTransform: 'uppercase' }}>
                          Establecimiento #{idx + 1}
                        </span>
                        <div style={{ display: 'flex', gap: '0.35rem' }}>
                          <button
                            type="button"
                            onClick={() => abrirModalEditarEstablecimiento(idx)}
                            className="btn-edit-outline"
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            onClick={() => handleEliminarEstablecimiento(idx)}
                            className="btn-danger-outline"
                          >
                            Eliminar
                          </button>
                        </div>
                      </div>

                      <h3 style={{ fontSize: '0.9375rem', fontWeight: 700, color: '#1e293b', marginBottom: '0.75rem' }}>
                        {est.nombreEstablecimiento}
                      </h3>

                      <div style={{ fontSize: '0.8125rem', color: '#475569', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <DollarSign size={14} color="#10b981" />
                          <span>
                            Ingresos:{' '}
                            <strong style={{ color: '#059669' }}>
                              {new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(est.ingresos)}
                            </strong>
                          </span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <Users size={14} color="#0284c7" />
                          <span>No. Empleados: <strong>{est.numeroEmpleados}</strong></span>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </main>

      {/* Modal para Crear / Editar Establecimiento */}
      {modalEstablecimientoAbierto && (
        <div style={{
          position: 'fixed',
          inset: 0,
          backgroundColor: 'rgba(15, 23, 42, 0.6)',
          backdropFilter: 'blur(2px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '1rem',
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            borderRadius: '10px',
            padding: '1.75rem',
            maxWidth: '460px',
            width: '100%',
            boxShadow: '0 20px 25px -5px rgba(0,0,0,0.3)',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, color: '#1e293b' }}>
                {estIndiceEdicion !== null ? 'Editar Establecimiento' : 'Nuevo Establecimiento'}
              </h3>
              <button
                type="button"
                onClick={() => setModalEstablecimientoAbierto(false)}
                style={{ background: 'transparent', color: '#94a3b8' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleGuardarEstablecimientoModal} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                  Nombre Establecimiento *
                </label>
                <input
                  type="text"
                  placeholder="Ej: Sucursal Norte Centro"
                  value={estNombre}
                  onChange={(e) => setEstNombre(e.target.value)}
                  className={estErrores.nombre ? 'input-error' : ''}
                />
                {estErrores.nombre && <span className="error-text">{estErrores.nombre}</span>}
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                  Ingresos * (COP - permite decimales)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="0.00"
                  value={estIngresos}
                  onChange={(e) => setEstIngresos(e.target.value)}
                  className={estErrores.ingresos ? 'input-error' : ''}
                />
                {estErrores.ingresos && <span className="error-text">{estErrores.ingresos}</span>}
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                  Número de Empleados *
                </label>
                <input
                  type="number"
                  min="0"
                  placeholder="0"
                  value={estEmpleados}
                  onChange={(e) => setEstEmpleados(e.target.value)}
                  className={estErrores.empleados ? 'input-error' : ''}
                />
                {estErrores.empleados && <span className="error-text">{estErrores.empleados}</span>}
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.75rem' }}>
                <button
                  type="button"
                  onClick={() => setModalEstablecimientoAbierto(false)}
                  className="btn-secondary"
                >
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">
                  {estIndiceEdicion !== null ? 'Guardar Cambios' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Footer Flotante Fijo con Totales (Fiel al prototipo de la Página 12) */}
      <div style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        backgroundColor: '#1e3a8a',
        color: '#ffffff',
        padding: '0.875rem 2rem',
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        zIndex: 40,
        boxShadow: '0 -4px 10px rgba(0,0,0,0.15)',
        gap: '1rem',
      }}>
        {/* Métricas calculadas en vivo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '2.5rem' }}>
          <div>
            <span style={{ display: 'block', fontSize: '0.7rem', color: '#93c5fd', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              Total Ingresos Formulario:
            </span>
            <span style={{ fontSize: '1.25rem', fontWeight: 800, color: '#ffffff' }}>
              {totalesCalculados.ingresosTexto}
            </span>
          </div>

          <div>
            <span style={{ display: 'block', fontSize: '0.7rem', color: '#93c5fd', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
              Cantidad de empleados:
            </span>
            <span style={{ fontSize: '1.25rem', fontWeight: 800, color: '#ffffff' }}>
              {totalesCalculados.cantidadEmpleados}
            </span>
          </div>
        </div>

        {/* Botón de Enviar Formulario */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <span style={{ fontSize: '0.8rem', color: '#bfdbfe' }}>
            Si ya ingresaste todos los datos, crea tu formulario aquí
          </span>
          <button
            type="button"
            disabled={guardando}
            onClick={handleSubmitGeneral}
            className="btn-primary"
            style={{
              padding: '0.75rem 1.75rem',
              fontSize: '0.9375rem',
              fontWeight: 700,
            }}
          >
            {guardando
              ? 'Procesando...'
              : esEdicion
              ? 'Actualizar Formulario'
              : 'Enviar Formulario'}
          </button>
        </div>
      </div>
    </div>
  );
};
