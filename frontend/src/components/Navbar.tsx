import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { User, LogOut, FileText, PlusCircle, Award } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const rolFormat = user?.rol === 'ADMINISTRADOR' ? 'Administrador' : 'Auxiliar de Registro';

  return (
    <header style={{
      backgroundColor: '#ffffff',
      borderBottom: '1px solid #e2e8f0',
      boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
      position: 'sticky',
      top: 0,
      zIndex: 50,
    }}>
      <div style={{
        maxWidth: '1360px',
        margin: '0 auto',
        padding: '0.75rem 1.5rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}>
        {/* Logo corporativo OL Software */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none' }}>
          <div style={{
            background: 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
            color: '#ffffff',
            fontWeight: 800,
            fontSize: '1.25rem',
            padding: '0.35rem 0.65rem',
            borderRadius: '6px',
            letterSpacing: '0.5px',
          }}>
            OL
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '1.125rem', fontWeight: 700, color: '#0f172a', lineHeight: 1.1 }}>OLSoftware</span>
            <span style={{ fontSize: '0.7rem', color: '#64748b', fontWeight: 500 }}>Software & Development</span>
          </div>
        </Link>

        {/* Enlaces de navegación (según prototipo) */}
        {isAuthenticated && (
          <nav style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
            <Link
              to="/"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.375rem',
                fontSize: '0.875rem',
                fontWeight: location.pathname === '/' ? 600 : 500,
                color: location.pathname === '/' ? '#0284c7' : '#475569',
                textDecoration: 'none',
              }}
            >
              <FileText size={16} />
              Lista Formulario
            </Link>

            <span style={{ color: '#cbd5e1' }}>|</span>

            <Link
              to="/formulario"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.375rem',
                fontSize: '0.875rem',
                fontWeight: location.pathname === '/formulario' ? 600 : 500,
                color: location.pathname === '/formulario' ? '#0284c7' : '#475569',
                textDecoration: 'none',
              }}
            >
              <PlusCircle size={16} />
              Crear Formulario
            </Link>

            <span style={{ color: '#cbd5e1' }}>|</span>

            <span style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.375rem',
              fontSize: '0.875rem',
              fontWeight: 500,
              color: '#64748b',
              cursor: 'default',
            }}>
              <Award size={16} />
              Beneficios por renovar
            </span>
          </nav>
        )}

        {/* Sección de Usuario & Rol */}
        {isAuthenticated && user ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
              <div style={{
                width: '38px',
                height: '38px',
                borderRadius: '50%',
                backgroundColor: '#f1f5f9',
                border: '1px solid #cbd5e1',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#475569',
              }}>
                <User size={20} />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>¡Bienvenido!</span>
                <span style={{ fontSize: '0.875rem', fontWeight: 700, color: '#1e293b' }}>{user.nombre}</span>
                <span style={{ fontSize: '0.7rem', color: '#0284c7', fontWeight: 600 }}>{rolFormat}</span>
              </div>
            </div>

            <button
              onClick={handleLogout}
              title="Cerrar sesión"
              style={{
                backgroundColor: '#f8fafc',
                border: '1px solid #e2e8f0',
                borderRadius: '6px',
                padding: '0.5rem',
                display: 'flex',
                alignItems: 'center',
                color: '#ef4444',
                cursor: 'pointer',
              }}
            >
              <LogOut size={18} />
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#64748b', fontSize: '0.875rem' }}>
            <Award size={16} />
            Beneficios por renovar
          </div>
        )}
      </div>
    </header>
  );
};
