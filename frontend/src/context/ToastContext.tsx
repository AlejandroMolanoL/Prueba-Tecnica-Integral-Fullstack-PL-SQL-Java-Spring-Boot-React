import React, { createContext, useContext, useState, useCallback } from 'react';
import { X, AlertCircle, CheckCircle2, Info } from 'lucide-react';

type ToastType = 'success' | 'error' | 'info';

interface ToastItem {
  id: number;
  message: string;
  type: ToastType;
}

interface ToastContextType {
  showToast: (message: string, type?: ToastType) => void;
  removeToast: (id: number) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback((message: string, type: ToastType = 'info') => {
    setToasts((prev) => {
      // Evitar duplicar alertas consecutivas que tengan exactamente el mismo mensaje
      if (prev.some((t) => t.message === message)) {
        return prev;
      }
      const id = Date.now() + Math.random();
      // Limitar a máximo 4 alertas visibles a la vez para no saturar la pantalla
      const nuevoListado = [...prev, { id, message, type }].slice(-4);

      setTimeout(() => {
        removeToast(id);
      }, 5000);

      return nuevoListado;
    });
  }, [removeToast]);

  const value = React.useMemo(() => ({ showToast, removeToast }), [showToast, removeToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-container" style={{
        position: 'fixed',
        top: '1rem',
        right: '1rem',
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column',
        gap: '0.625rem',
        maxWidth: '420px',
        width: 'calc(100% - 2rem)',
        pointerEvents: 'none',
      }}>
        {toasts.map((toast) => {
          let IconComponent = Info;
          if (toast.type === 'success') IconComponent = CheckCircle2;
          if (toast.type === 'error') IconComponent = AlertCircle;

          return (
            <div
              key={toast.id}
              className={`toast toast-${toast.type}`}
              style={{
                pointerEvents: 'auto',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '0.75rem',
                padding: '0.75rem 1rem',
                borderRadius: '8px',
                boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.2), 0 4px 6px -2px rgba(0, 0, 0, 0.1)',
                animation: 'slideIn 0.25s ease-out',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem', flex: 1, minWidth: 0 }}>
                <IconComponent size={18} style={{ flexShrink: 0 }} />
                <span style={{ fontSize: '0.8125rem', lineHeight: 1.4, wordBreak: 'break-word' }}>
                  {toast.message}
                </span>
              </div>

              {/* Botón X para cerrar alerta */}
              <button
                type="button"
                onClick={() => removeToast(toast.id)}
                title="Cerrar notificación"
                aria-label="Cerrar"
                style={{
                  background: 'rgba(255, 255, 255, 0.2)',
                  border: 'none',
                  borderRadius: '50%',
                  width: '24px',
                  height: '24px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: 'pointer',
                  color: '#ffffff',
                  flexShrink: 0,
                  transition: 'background-color 0.15s',
                }}
                onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.35)')}
                onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.2)')}
              >
                <X size={14} />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast debe ser utilizado dentro de un ToastProvider');
  }
  return context;
};
