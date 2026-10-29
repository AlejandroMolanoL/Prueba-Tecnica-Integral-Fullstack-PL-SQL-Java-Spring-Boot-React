import { api } from './api';
import type { ApiResponse, Comerciante, EstadoComerciante, PaginatedResponse } from '../types';

export interface ComercianteFiltros {
  nombre?: string;
  municipio?: string;
  fechaRegistro?: string;
  estado?: string;
  pagina?: number;
  registrosPorPagina?: number;
}

export const comercianteService = {
  getComerciantes: async (filtros: ComercianteFiltros = {}): Promise<PaginatedResponse<Comerciante>> => {
    const params: Record<string, string | number> = {
      pagina: filtros.pagina || 1,
      registrosPorPagina: filtros.registrosPorPagina || 5,
    };

    if (filtros.nombre?.trim()) params.nombre = filtros.nombre.trim();
    if (filtros.municipio?.trim()) params.municipio = filtros.municipio.trim();
    if (filtros.fechaRegistro) params.fechaRegistro = filtros.fechaRegistro;
    if (filtros.estado?.trim()) params.estado = filtros.estado.trim();

    const response = await api.get<ApiResponse<PaginatedResponse<Comerciante>>>('/comerciantes', { params });
    return response.data.datos;
  },

  getById: async (id: number): Promise<Comerciante> => {
    const response = await api.get<ApiResponse<Comerciante>>(`/comerciantes/${id}`);
    return response.data.datos;
  },

  create: async (data: Partial<Comerciante>): Promise<Comerciante> => {
    const response = await api.post<ApiResponse<Comerciante>>('/comerciantes', data);
    return response.data.datos;
  },

  update: async (id: number, data: Partial<Comerciante>): Promise<Comerciante> => {
    const response = await api.put<ApiResponse<Comerciante>>(`/comerciantes/${id}`, data);
    return response.data.datos;
  },

  cambiarEstado: async (id: number, nuevoEstado: EstadoComerciante): Promise<Comerciante> => {
    const response = await api.patch<ApiResponse<Comerciante>>(`/comerciantes/${id}/estado`, { nuevoEstado });
    return response.data.datos;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete<ApiResponse<void>>(`/comerciantes/${id}`);
  },

  descargarReporteCsv: async (): Promise<void> => {
    const response = await api.get('/comerciantes/reporte/csv', {
      responseType: 'blob',
    });

    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'reporte_comerciantes.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },

  descargarFichaPdf: async (id: number, nombreComerciante: string): Promise<void> => {
    const response = await api.get(`/comerciantes/${id}/ficha/pdf`, {
      responseType: 'blob',
    });

    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    const sanitizedName = nombreComerciante.replace(/[^a-zA-Z0-9]/g, '_');
    link.setAttribute('download', `Ficha_${sanitizedName}_${id}.pdf`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  },
};
