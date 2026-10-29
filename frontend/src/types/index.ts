export type RolUsuario = 'ADMINISTRADOR' | 'AUXILIAR_REGISTRO';

export type EstadoComerciante = 'Registrado' | 'Activo' | 'Inactivo' | 'Suspendido' | 'Cancelado';

export interface Usuario {
  nombre: string;
  correoElectronico: string;
  rol: RolUsuario;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  expiraEn: number;
  nombre: string;
  correoElectronico: string;
  rol: RolUsuario;
}

export interface Establecimiento {
  id?: number;
  comercianteId?: number;
  nombreEstablecimiento: string;
  ingresos: number;
  numeroEmpleados: number;
  fechaActualizacion?: string;
  usuarioActualizacion?: string;
}

export interface Comerciante {
  id: number;
  nombreRazonSocial: string;
  departamento: string;
  municipio: string;
  telefono?: string;
  correoElectronico?: string;
  fechaRegistro: string;
  estado: EstadoComerciante;
  cantidadEstablecimientos: number;
  totalActivos: number;
  cantidadEmpleados: number;
  establecimientos?: Establecimiento[];
  fechaActualizacion?: string;
  usuarioActualizacion?: string;
}

export interface ComercianteFormData {
  nombreRazonSocial: string;
  departamento: string;
  municipio: string;
  telefono: string;
  correoElectronico: string;
  fechaRegistro: string;
  estado: EstadoComerciante;
  poseeEstablecimientos: boolean;
  establecimientos: Establecimiento[];
}

export interface Departamento {
  id: number;
  codigoDane: string;
  nombre: string;
}

export interface Municipio {
  id: number;
  departamentoId: number;
  codigoDane: string;
  nombre: string;
}

export interface PaginatedResponse<T> {
  contenido: T[];
  paginaActual: number;
  registrosPorPagina: number;
  totalRegistros: number;
  totalPaginas: number;
  esPrimera: boolean;
  esUltima: boolean;
}

export interface ApiResponse<T> {
  exito: boolean;
  mensaje: string;
  datos: T;
  timestamp: string;
}
