export interface User {
  id: number;
  username: string;
  password?: string;
  role: 'ADMIN' | 'USER' | 'ROLE_ADMIN' | 'ROLE_USER';
  fullName: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  isActive?: boolean;
  createdAt?: string;
  lastLoginAt?: string;
}

export interface AuthResponse {
  id: number;
  username: string;
  fullName: string;
  role: 'ADMIN' | 'USER' | 'ROLE_ADMIN' | 'ROLE_USER';
  message?: string;
}
