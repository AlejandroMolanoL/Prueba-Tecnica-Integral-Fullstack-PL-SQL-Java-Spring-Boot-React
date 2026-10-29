import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { Navbar } from '../components/Navbar';
import { Lock, Mail, AlertCircle, ShieldCheck } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [aceptaTerminos, setAceptaTerminos] = useState(false);
  const [cargando, setCargando] = useState(false);
  const [errorLocal, setErrorLocal] = useState<string | null>(null);

  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorLocal(null);

    if (!correo.trim() || !contrasena.trim()) {
      setErrorLocal('Por favor ingrese su correo electrónico y contraseña.');
      return;
    }

    if (!aceptaTerminos) {
      setErrorLocal('Debe aceptar los términos y condiciones antes de iniciar sesión.');
      return;
    }

    setCargando(true);
    try {
      await login(correo, contrasena);
      showToast('Inicio de sesión exitoso. ¡Bienvenido!', 'success');
      navigate('/');
    } catch (err: any) {
      const msg = err.response?.data?.mensaje || 'Credenciales incorrectas. Verifique su correo y contraseña.';
      setErrorLocal(msg);
      showToast(msg, 'error');
    } finally {
      setCargando(false);
    }
  };

  const cargarCredenciales = (rol: 'admin' | 'aux') => {
    if (rol === 'admin') {
      setCorreo('admin@olsoftware.com');
      setContrasena('Admin123*');
      setAceptaTerminos(true);
    } else {
      setCorreo('auxiliar@olsoftware.com');
      setContrasena('Auxiliar123*');
      setAceptaTerminos(true);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />

      {/* Contenedor principal con fondo personalizado estilizado */}
      <div style={{
        flex: 1,
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '2rem 1rem',
        backgroundImage: 'radial-gradient(circle at 10% 20%, rgba(2, 132, 199, 0.08) 0%, rgba(225, 29, 72, 0.05) 90%), url("https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=1920&q=80")',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }}>
        {/* Capa de desenfoque y opacidad sobre el fondo */}
        <div style={{
          position: 'absolute',
          inset: 0,
          backdropFilter: 'blur(3px)',
          backgroundColor: 'rgba(15, 23, 42, 0.45)',
        }} />

        {/* Tarjeta de Login centrada (Fiel al prototipo de Reto 12) */}
        <div style={{
          position: 'relative',
          zIndex: 10,
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderRadius: '12px',
          padding: '2.5rem 2rem',
          width: '100%',
          maxWidth: '440px',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2)',
          border: '1px solid rgba(255, 255, 255, 0.3)',
        }}>
          <h2 style={{
            fontSize: '1.25rem',
            fontWeight: 700,
            textAlign: 'center',
            color: '#1e293b',
            marginBottom: '0.5rem',
          }}>
            Debes iniciar sesión para acceder a la plataforma
          </h2>

          <p style={{
            fontSize: '0.8rem',
            color: '#64748b',
            textAlign: 'center',
            marginBottom: '1.75rem',
            lineHeight: 1.4,
          }}>
            Digita tu documento de identidad del propietario o representante legal y la contraseña
          </p>

          {errorLocal && (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              backgroundColor: '#fef2f2',
              color: '#dc2626',
              padding: '0.75rem',
              borderRadius: '6px',
              fontSize: '0.8rem',
              marginBottom: '1.25rem',
              border: '1px solid #fecaca',
            }}>
              <AlertCircle size={16} />
              <span>{errorLocal}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.125rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.375rem' }}>
                Documento / Correo electrónico *
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  type="email"
                  value={correo}
                  onChange={(e) => setCorreo(e.target.value)}
                  placeholder="ejemplo@comercio.co"
                  required
                  style={{ paddingLeft: '2.25rem' }}
                />
                <Mail size={16} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: '#475569', marginBottom: '0.375rem' }}>
                Contraseña *
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  value={contrasena}
                  onChange={(e) => setContrasena(e.target.value)}
                  placeholder="••••••••"
                  required
                  style={{ paddingLeft: '2.25rem' }}
                />
                <Lock size={16} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }} />
              </div>
            </div>

            {/* Checkbox Obligatorio Términos y Condiciones (Reto 12) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.25rem' }}>
              <input
                type="checkbox"
                id="terminos"
                checked={aceptaTerminos}
                onChange={(e) => setAceptaTerminos(e.target.checked)}
                style={{ width: 'auto', cursor: 'pointer' }}
              />
              <label htmlFor="terminos" style={{ fontSize: '0.8rem', color: '#475569', cursor: 'pointer', userSelect: 'none' }}>
                Acepto términos y condiciones
              </label>
            </div>

            {/* Botón Magenta de Inicio de Sesión */}
            <button
              type="submit"
              disabled={cargando || !aceptaTerminos}
              className="btn-primary"
              style={{
                marginTop: '0.5rem',
                padding: '0.75rem',
                fontSize: '0.925rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.5rem',
              }}
            >
              {cargando ? 'Iniciando sesión...' : 'Iniciar sesión'}
            </button>
          </form>

          {/* Acceso rápido para calificación y pruebas */}
          <div style={{
            marginTop: '1.75rem',
            paddingTop: '1.25rem',
            borderTop: '1px solid #e2e8f0',
            textAlign: 'center',
          }}>
            <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.35rem', marginBottom: '0.625rem' }}>
              <ShieldCheck size={14} color="#0284c7" /> Cuentas para evaluación rápida:
            </span>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
              <button
                type="button"
                onClick={() => cargarCredenciales('admin')}
                style={{
                  fontSize: '0.7rem',
                  padding: '0.35rem 0.65rem',
                  backgroundColor: '#eff6ff',
                  color: '#1d4ed8',
                  borderRadius: '4px',
                  border: '1px solid #bfdbfe',
                }}
              >
                Rol Administrador
              </button>
              <button
                type="button"
                onClick={() => cargarCredenciales('aux')}
                style={{
                  fontSize: '0.7rem',
                  padding: '0.35rem 0.65rem',
                  backgroundColor: '#f8fafc',
                  color: '#475569',
                  borderRadius: '4px',
                  border: '1px solid #cbd5e1',
                }}
              >
                Rol Auxiliar
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
