import type { TimestampedEntity } from '@/shared/types/common';

export type UserRole = 'ADMIN' | 'MEMBER';

export interface UserSummary {
  id: number;
  username: string;
  displayName: string;
}

export interface User extends TimestampedEntity {
  id: number;
  username: string;
  displayName: string;
  email: string;
  role: UserRole;
}

export interface UserFilter {
  keyword?: string;
  role?: UserRole;
}

export interface CreateUserPayload {
  username: string;
  displayName: string;
  email: string;
  role?: UserRole;
}

export interface UpdateUserPayload {
  displayName: string;
  email: string;
  role: UserRole;
}
