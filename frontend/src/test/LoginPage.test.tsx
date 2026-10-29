import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { LoginPage } from '../pages/LoginPage';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { ToastProvider } from '../context/ToastContext';

describe('LoginPage Component (Reto 12)', () => {
  const renderComponent = () =>
    render(
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <LoginPage />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    );

  it('debe renderizar el título, campos de entrada y el checkbox de términos y condiciones', () => {
    renderComponent();

    expect(screen.getByText(/Debes iniciar sesión para acceder a la plataforma/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/ejemplo@comercio.co/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/••••••••/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Acepto términos y condiciones/i)).toBeInTheDocument();
  });

  it('el botón de inicio de sesión debe estar deshabilitado hasta que se acepten los términos y condiciones', () => {
    renderComponent();

    const boton = screen.getByRole('button', { name: /Iniciar sesión/i });
    const checkbox = screen.getByLabelText(/Acepto términos y condiciones/i) as HTMLInputElement;

    expect(checkbox.checked).toBe(false);
    expect(boton).toBeDisabled();

    // Marcamos el checkbox
    fireEvent.click(checkbox);
    expect(checkbox.checked).toBe(true);
    expect(boton).not.toBeDisabled();
  });

  it('debe permitir autocompletar credenciales con los botones de prueba rápida', () => {
    renderComponent();

    const botonAdmin = screen.getByRole('button', { name: /Rol Administrador/i });
    fireEvent.click(botonAdmin);

    const inputCorreo = screen.getByPlaceholderText(/ejemplo@comercio.co/i) as HTMLInputElement;
    expect(inputCorreo.value).toBe('admin@olsoftware.com');
  });
});
