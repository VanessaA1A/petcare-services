export interface UserModel {
  id: number;
  username: string;
  email: string;
  rol?: string | null;
  created_at?: string;
  last_login?: string | null;
  is_active?: boolean;
}

export interface SessionModel {
  id: number;
  token_sesion: string;
  fecha_inicio?: string;
}

export interface ActivityModel {
  id: number;
  sesion_id?: number;
  usuario_id?: number;
  tipo_actividad?: string;
  descripcion?: string | null;
  ip_address?: string | null;
  fecha_hora?: string;
}
