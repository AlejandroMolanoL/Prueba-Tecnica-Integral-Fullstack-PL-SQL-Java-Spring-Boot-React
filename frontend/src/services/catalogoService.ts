import { api } from './api';
import type { ApiResponse, Departamento, Municipio } from '../types';

export const catalogoService = {
  getDepartamentos: async (): Promise<Departamento[]> => {
    const response = await api.get<ApiResponse<Departamento[]>>('/departamentos');
    return response.data.datos;
  },

  getMunicipiosPorDepartamento: async (departamentoId: number): Promise<Municipio[]> => {
    const response = await api.get<ApiResponse<Municipio[]>>(`/departamentos/${departamentoId}/municipios`);
    return response.data.datos;
  },

  getMunicipiosPorNombre: async (nombreDepartamento: string): Promise<Municipio[]> => {
    const response = await api.get<ApiResponse<Municipio[]>>('/municipios', {
      params: { departamento: nombreDepartamento },
    });
    return response.data.datos;
  },
};
