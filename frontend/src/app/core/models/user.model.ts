export type Role = 'ADMIN' | 'USER';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
}
