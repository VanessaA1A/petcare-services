export type ApiRole = 'OWNER' | 'CAREGIVER' | null;

export interface UserViewModel {
  id: number;
  username: string;
  email: string;
  rol?: ApiRole;
}

export interface SessionViewModel {
  id: number;
  token: string;
}
