import { api } from './api';
import type { ApiResponse, Establecimiento } from '../types';

export const establecimientoService = {
  getByComerciante: async (comercianteId: number): Promise<Establecimiento[]> => {
    const response = await api.get<ApiResponse<Establecimiento[]>>(`/establecimientos/comerciante/${comercianteId}`);
    return response.data.datos;
  },

  create: async (establecimiento: Establecimiento): Promise<Establecimiento> => {
    const response = await api.post<ApiResponse<Establecimiento>>('/establecimientos', establecimiento);
    return response.data.datos;
  },

  update: async (id: number, establecimiento: Establecimiento): Promise<Establecimiento> => {
    const response = await api.put<ApiResponse<Establecimiento>>(`/establecimientos/${id}`, establecimiento);
    return response.data.datos;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`/establecimientos/${id}`);
  },
};
