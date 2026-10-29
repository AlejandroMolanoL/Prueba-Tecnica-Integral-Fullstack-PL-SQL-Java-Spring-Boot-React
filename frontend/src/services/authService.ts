import { api } from './api';
import type { ApiResponse, AuthResponse, Usuario } from '../types';

export const authService = {
  login: async (correoElectronico: string, contrasena: string): Promise<AuthResponse> => {
    // Reto 12 / OWASP: Protección de credenciales en tránsito
    const payload = {
      correoElectronico: correoElectronico.trim().toLowerCase(),
      contrasena: contrasena.trim(),
    };

    const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', payload);
    const data = response.data.datos;

    // Almacenamiento seguro en sessionStorage (no en localStorage persistente ni window global)
    sessionStorage.setItem('token', data.token);
    sessionStorage.setItem(
      'user',
      JSON.stringify({
        nombre: data.nombre,
        correoElectronico: data.correoElectronico,
        rol: data.rol,
      })
    );

    return data;
  },

  logout: () => {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
  },

  getCurrentUser: (): Usuario | null => {
    const userStr = sessionStorage.getItem('user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as Usuario;
    } catch {
      return null;
    }
  },

  getToken: (): string | null => {
    return sessionStorage.getItem('token');
  },

  isAuthenticated: (): boolean => {
    return !!sessionStorage.getItem('token');
  },
};
