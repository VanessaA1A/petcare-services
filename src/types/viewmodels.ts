export type ApiRole = 'OWNER' | 'CAREGIVER' | null;

export interface UserViewModel {
  id: number;
  username: string;
  email: string;
  rol: ApiRole;
}

export interface SessionViewModel {
  id: number;
  token: string;
}

export interface PetViewModel {
  id: string;
  owner_id: number;
  name: string;
  species?: string | null;
  breed: string;
  size: string;
  age?: number | null;
  weight?: number | null;
  description?: string | null;
  created_at?: string;
  updated_at?: string;
}
