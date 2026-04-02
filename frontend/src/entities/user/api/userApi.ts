import { apiRequest } from '@/shared/api/client';

import type {
  CreateUserPayload,
  UpdateUserPayload,
  User,
  UserFilter,
} from '@/entities/user/model/types';

export function getUsers(filters: UserFilter = {}) {
  return apiRequest<User[]>('/users', {
    query: filters,
  });
}

export function getUser(userId: number | string) {
  return apiRequest<User>(`/users/${userId}`);
}

export function createUser(payload: CreateUserPayload) {
  return apiRequest<User>('/users', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function updateUser(userId: number | string, payload: UpdateUserPayload) {
  return apiRequest<User>(`/users/${userId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
}

export function deleteUser(userId: number | string) {
  return apiRequest<void>(`/users/${userId}`, {
    method: 'DELETE',
  });
}
