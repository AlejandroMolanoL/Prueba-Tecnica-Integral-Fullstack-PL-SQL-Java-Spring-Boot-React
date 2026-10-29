import { describe, it, expect, beforeEach } from 'vitest';
import { authService } from '../services/authService';

describe('AuthService (Seguridad OWASP & Sesión)', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('debe limpiar token y datos de usuario al ejecutar logout', () => {
    sessionStorage.setItem('token', 'mock.jwt.token');
    sessionStorage.setItem('user', JSON.stringify({ nombre: 'Test', rol: 'ADMINISTRADOR' }));

    expect(authService.isAuthenticated()).toBe(true);

    authService.logout();

    expect(authService.isAuthenticated()).toBe(false);
    expect(authService.getToken()).toBeNull();
    expect(authService.getCurrentUser()).toBeNull();
  });

  it('debe recuperar usuario actual almacenado en sessionStorage correctamente', () => {
    const usuarioMock = {
      nombre: 'Alejandro Admin',
      correoElectronico: 'admin@olsoftware.com',
      rol: 'ADMINISTRADOR' as const,
    };

    sessionStorage.setItem('user', JSON.stringify(usuarioMock));
    sessionStorage.setItem('token', 'jwt-token-valido');

    const recuperado = authService.getCurrentUser();
    expect(recuperado).not.toBeNull();
    expect(recuperado?.nombre).toBe('Alejandro Admin');
    expect(recuperado?.rol).toBe('ADMINISTRADOR');
  });
});
