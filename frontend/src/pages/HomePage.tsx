import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { Footer } from '../components/Footer';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { comercianteService, type ComercianteFiltros } from '../services/comercianteService';
import type { Comerciante, EstadoComerciante } from '../types';
import {
  Plus,
  Download,
  Edit2,
  Trash2,
  FileDown,
  ToggleLeft,
  ToggleRight,
  Filter,
  RotateCcw,
  Search,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

export const HomePage: React.FC = () => {
  const [comerciantes, setComerciantes] = useState<Comerciante[]>([]);
  const [cargando, setCargando] = useState(false);
  const [totalRegistros, setTotalRegistros] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(1);
  const [paginaActual, setPaginaActual] = useState(1);
  const [registrosPorPagina, setRegistrosPorPagina] = useState(5);

  // Filtros
  const [filtroNombre, setFiltroNombre] = useState('');
  const [filtroMunicipio, setFiltroMunicipio] = useState('');
  const [filtroFecha, setFiltroFecha] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');

  // Modal confirmación eliminar
  const [comercianteAEliminar, setComercianteAEliminar] = useState<Comerciante | null>(null);
  const [eliminando, setEliminando] = useState(false);

  const { isAdmin } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const cargarComerciantes = useCallback(async () => {
    setCargando(true);
    try {
      const filtros: ComercianteFiltros = {
        pagina: paginaActual,
        registrosPorPagina: registrosPorPagina,
        nombre: filtroNombre,
        municipio: filtroMunicipio,
        fechaRegistro: filtroFecha,
        estado: filtroEstado,
      };

      const data = await comercianteService.getComerciantes(filtros);
      setComerciantes(data.contenido);
      setTotalRegistros(data.totalRegistros);
      setTotalPaginas(data.totalPaginas);
    } catch (err: any) {
      showToast('Error al consultar los comerciantes: ' + (err.response?.data?.mensaje || err.message), 'error');
    } finally {
      setCargando(false);
    }
  }, [paginaActual, registrosPorPagina, filtroNombre, filtroMunicipio, filtroFecha, filtroEstado, showToast]);

  useEffect(() => {
    cargarComerciantes();
  }, [cargarComerciantes]);

  const handleBuscar = (e: React.FormEvent) => {
    e.preventDefault();
    setPaginaActual(1);
    cargarComerciantes();
  };

  const handleLimpiarFiltros = () => {
    setFiltroNombre('');
    setFiltroMunicipio('');
    setFiltroFecha('');
    setFiltroEstado('');
    setPaginaActual(1);
  };

  const handleToggleEstado = async (comerciante: Comerciante) => {
    const nuevoEstado: EstadoComerciante = comerciante.estado === 'Activo' ? 'Inactivo' : 'Activo';
    try {
      await comercianteService.cambiarEstado(comerciante.id, nuevoEstado);
      showToast(`Estado de "${comerciante.nombreRazonSocial}" actualizado a ${nuevoEstado}`, 'success');
      cargarComerciantes();
    } catch (err: any) {
      showToast('Error al cambiar el estado: ' + (err.response?.data?.mensaje || err.message), 'error');
    }
  };

  const handleConfirmarEliminar = async () => {
    if (!comercianteAEliminar) return;
    setEliminando(true);
    try {
      await comercianteService.delete(comercianteAEliminar.id);
      showToast(`Comerciante "${comercianteAEliminar.nombreRazonSocial}" eliminado correctamente`, 'success');
      setComercianteAEliminar(null);
      cargarComerciantes();
    } catch (err: any) {
      showToast('Error al eliminar comerciante: ' + (err.response?.data?.mensaje || err.message), 'error');
    } finally {
      setEliminando(false);
    }
  };

  const handleDescargarCsv = async () => {
    if (!isAdmin) {
      showToast('Acceso restringido: Solo los administradores pueden descargar el reporte CSV.', 'error');
      return;
    }
    try {
      showToast('Generando reporte CSV delimitado por pipe...', 'info');
      await comercianteService.descargarReporteCsv();
      showToast('Reporte CSV descargado exitosamente.', 'success');
    } catch (err: any) {
      showToast('Error al descargar reporte CSV: ' + (err.response?.data?.mensaje || err.message), 'error');
    }
  };

  const handleDescargarPdf = async (comerciante: Comerciante) => {
    if (!isAdmin) {
      showToast('Acceso restringido: Solo los administradores pueden descargar la Ficha en PDF.', 'error');
      return;
    }
    try {
      showToast(`Generando Ficha en PDF para "${comerciante.nombreRazonSocial}"...`, 'info');
      await comercianteService.descargarFichaPdf(comerciante.id, comerciante.nombreRazonSocial);
      showToast('Ficha PDF descargada exitosamente.', 'success');
    } catch (err: any) {
      showToast('Error al descargar Ficha PDF: ' + (err.response?.data?.mensaje || err.message), 'error');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />

      <main style={{ flex: 1, padding: '2rem 1.5rem', maxWidth: '1360px', margin: '0 auto', width: '100%' }}>
        {/* Encabezado y Botones Superiores (Fiel al prototipo de la Página 10) */}
        <div style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '1rem',
          marginBottom: '1.5rem',
        }}>
          <div>
            <h1 style={{ fontSize: '1.375rem', fontWeight: 700, color: '#1e293b' }}>
              Lista Formularios Creados
            </h1>
            <p style={{ fontSize: '0.8125rem', color: '#64748b' }}>
              Consolidado de comerciantes registrados y sus establecimientos de comercio
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
            <button
              onClick={() => navigate('/formulario')}
              className="btn-primary"
              style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
            >
              <Plus size={16} />
              Crear Formulario Nuevo
            </button>

            {isAdmin && (
              <button
                onClick={handleDescargarCsv}
                className="btn-secondary"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  color: '#be123c',
                  borderColor: '#f43f5e',
                }}
              >
                <Download size={16} />
                Descargar Reporte en CSV
              </button>
            )}
          </div>
        </div>

        {/* Panel de Filtros */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          padding: '1.25rem',
          border: '1px solid #e2e8f0',
          boxShadow: '0 1px 3px rgba(0,0,0,0.04)',
          marginBottom: '1.5rem',
        }}>
          <form onSubmit={handleBuscar} style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1rem',
            alignItems: 'flex-end',
          }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Nombre o Razón Social
              </label>
              <input
                type="text"
                placeholder="Buscar por razón social..."
                value={filtroNombre}
                onChange={(e) => setFiltroNombre(e.target.value)}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Municipio
              </label>
              <input
                type="text"
                placeholder="Ej: Medellín, Cali..."
                value={filtroMunicipio}
                onChange={(e) => setFiltroMunicipio(e.target.value)}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Fecha de Registro
              </label>
              <input
                type="date"
                value={filtroFecha}
                onChange={(e) => setFiltroFecha(e.target.value)}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.35rem' }}>
                Estado
              </label>
              <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
                <option value="">Todos los estados</option>
                <option value="Registrado">Registrado</option>
                <option value="Activo">Activo</option>
                <option value="Inactivo">Inactivo</option>
                <option value="Suspendido">Suspendido</option>
                <option value="Cancelado">Cancelado</option>
              </select>
            </div>

            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                type="submit"
                className="btn-secondary"
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.375rem',
                  backgroundColor: '#0284c7',
                  color: '#ffffff',
                  borderColor: '#0284c7',
                }}
              >
                <Search size={16} />
                Filtrar
              </button>

              <button
                type="button"
                onClick={handleLimpiarFiltros}
                className="btn-secondary"
                title="Limpiar filtros"
                style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}
              >
                <RotateCcw size={16} />
              </button>
            </div>
          </form>
        </div>

        {/* Tabla de Comerciantes (Fiel al prototipo de la Página 10) */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          border: '1px solid #e2e8f0',
          boxShadow: '0 1px 3px rgba(0,0,0,0.04)',
          overflow: 'hidden',
        }}>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
              <thead>
                <tr style={{
                  backgroundColor: '#38bdf8',
                  color: '#ffffff',
                  fontWeight: 600,
                  borderBottom: '2px solid #0284c7',
                }}>
                  <th style={{ padding: '0.75rem 1rem' }}>Razón Social</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Teléfono</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Correo Electrónico</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Fecha Registro</th>
                  <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>No. Establecimientos</th>
                  <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>Estado</th>
                  <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {cargando ? (
                  <tr>
                    <td colSpan={7} style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                      Cargando información de comerciantes...
                    </td>
                  </tr>
                ) : comerciantes.length === 0 ? (
                  <tr>
                    <td colSpan={7} style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                      No se encontraron comerciantes con los criterios ingresados.
                    </td>
                  </tr>
                ) : (
                  comerciantes.map((c, idx) => (
                    <tr
                      key={c.id}
                      style={{
                        backgroundColor: idx % 2 === 0 ? '#ffffff' : '#f8fafc',
                        borderBottom: '1px solid #f1f5f9',
                        transition: 'background-color 0.15s',
                      }}
                    >
                      <td style={{ padding: '0.875rem 1rem', fontWeight: 600, color: '#1e293b' }}>
                        {c.nombreRazonSocial}
                      </td>
                      <td style={{ padding: '0.875rem 1rem', color: '#475569' }}>
                        {c.telefono || 'N/A'}
                      </td>
                      <td style={{ padding: '0.875rem 1rem', color: '#475569' }}>
                        {c.correoElectronico || 'N/A'}
                      </td>
                      <td style={{ padding: '0.875rem 1rem', color: '#475569' }}>
                        {c.fechaRegistro}
                      </td>
                      <td style={{ padding: '0.875rem 1rem', textAlign: 'center', fontWeight: 600, color: '#0284c7' }}>
                        {c.cantidadEstablecimientos}
                      </td>
                      <td style={{ padding: '0.875rem 1rem', textAlign: 'center' }}>
                        <span className={`badge badge-${c.estado.toLowerCase()}`}>
                          {c.estado}
                        </span>
                      </td>
                      <td style={{ padding: '0.875rem 1rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}>
                          {/* Editar */}
                          <button
                            onClick={() => navigate(`/formulario?id=${c.id}`)}
                            title="Editar registro"
                            style={{
                              background: 'transparent',
                              color: '#0284c7',
                              padding: '0.3rem',
                              borderRadius: '4px',
                            }}
                          >
                            <Edit2 size={16} />
                          </button>

                          {/* Activar/Inactivar (PATCH Reto 08) */}
                          <button
                            onClick={() => handleToggleEstado(c)}
                            title={c.estado === 'Activo' ? 'Inactivar comerciante' : 'Activar comerciante'}
                            style={{
                              background: 'transparent',
                              color: c.estado === 'Activo' ? '#10b981' : '#94a3b8',
                              padding: '0.3rem',
                              borderRadius: '4px',
                            }}
                          >
                            {c.estado === 'Activo' ? <ToggleRight size={20} /> : <ToggleLeft size={20} />}
                          </button>

                          {/* Eliminar (Solo Admin) */}
                          {isAdmin && (
                            <button
                              onClick={() => setComercianteAEliminar(c)}
                              title="Eliminar comerciante (Solo Administrador)"
                              style={{
                                background: 'transparent',
                                color: '#ef4444',
                                padding: '0.3rem',
                                borderRadius: '4px',
                              }}
                            >
                              <Trash2 size={16} />
                            </button>
                          )}

                          {/* Descargar PDF Ficha (Solo Admin) */}
                          {isAdmin && (
                            <button
                              onClick={() => handleDescargarPdf(c)}
                              title="Descargar Ficha en PDF (Reto 11)"
                              style={{
                                background: 'transparent',
                                color: '#be123c',
                                padding: '0.3rem',
                                borderRadius: '4px',
                              }}
                            >
                              <FileDown size={16} />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Paginador (Fiel al diseño del prototipo de la Página 10) */}
          <div style={{
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '1rem',
            borderTop: '1px solid #e2e8f0',
            backgroundColor: '#ffffff',
            gap: '1rem',
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8125rem', color: '#64748b' }}>
              <span>Items:</span>
              <select
                value={registrosPorPagina}
                onChange={(e) => {
                  setRegistrosPorPagina(Number(e.target.value));
                  setPaginaActual(1);
                }}
                style={{ width: 'auto', padding: '0.25rem 0.5rem' }}
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
              </select>
              <span>de {totalRegistros} registros totales</span>
            </div>

            {/* Navegación por páginas con botones idénticos al prototipo */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <button
                disabled={paginaActual <= 1}
                onClick={() => setPaginaActual((p) => Math.max(p - 1, 1))}
                style={{
                  backgroundColor: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '4px',
                  padding: '0.35rem 0.6rem',
                  display: 'flex',
                  alignItems: 'center',
                }}
              >
                <ChevronLeft size={16} />
              </button>

              {Array.from({ length: totalPaginas }, (_, i) => i + 1)
                .slice(Math.max(0, paginaActual - 3), Math.min(totalPaginas, paginaActual + 2))
                .map((num) => (
                  <button
                    key={num}
                    onClick={() => setPaginaActual(num)}
                    style={{
                      backgroundColor: num === paginaActual ? '#38bdf8' : '#ffffff',
                      color: num === paginaActual ? '#ffffff' : '#475569',
                      border: '1px solid #cbd5e1',
                      borderRadius: '4px',
                      padding: '0.35rem 0.75rem',
                      fontSize: '0.8125rem',
                      fontWeight: num === paginaActual ? 700 : 500,
                    }}
                  >
                    {num}
                  </button>
                ))}

              <button
                disabled={paginaActual >= totalPaginas}
                onClick={() => setPaginaActual((p) => Math.min(p + 1, totalPaginas))}
                style={{
                  backgroundColor: '#f1f5f9',
                  border: '1px solid #cbd5e1',
                  borderRadius: '4px',
                  padding: '0.35rem 0.6rem',
                  display: 'flex',
                  alignItems: 'center',
                }}
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        </div>
      </main>

      {/* Modal de confirmación para eliminar comerciante */}
      {comercianteAEliminar && (
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
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700, color: '#1e293b', marginBottom: '0.75rem' }}>
              Confirmar Eliminación
            </h3>
            <p style={{ fontSize: '0.875rem', color: '#64748b', lineHeight: 1.5, marginBottom: '1.5rem' }}>
              ¿Está seguro que desea eliminar al comerciante <strong>"{comercianteAEliminar.nombreRazonSocial}"</strong>?
              Esta acción también eliminará todos sus establecimientos vinculados de manera permanente.
            </p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                onClick={() => setComercianteAEliminar(null)}
                className="btn-secondary"
                disabled={eliminando}
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleConfirmarEliminar}
                className="btn-primary"
                disabled={eliminando}
                style={{ backgroundColor: '#dc2626' }}
              >
                {eliminando ? 'Eliminando...' : 'Sí, Eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
};
