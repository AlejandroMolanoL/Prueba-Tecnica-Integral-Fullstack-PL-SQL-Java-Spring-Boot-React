import React, { createContext, useContext, useState } from 'react';
import type { Usuario } from '../types';
import { authService } from '../services/authService';

interface AuthContextType {
  user: Usuario | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (correoElectronico: string, contrasena: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<Usuario | null>(() => authService.getCurrentUser());
  const [token, setToken] = useState<string | null>(() => authService.getToken());

  const login = async (correoElectronico: string, contrasena: string) => {
    const authData = await authService.login(correoElectronico, contrasena);
    setUser({
      nombre: authData.nombre,
      correoElectronico: authData.correoElectronico,
      rol: authData.rol,
    });
    setToken(authData.token);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
  };

  const isAuthenticated = !!token && !!user;
  const isAdmin = user?.rol === 'ADMINISTRADOR';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isAdmin,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un AuthProvider');
  }
  return context;
};
