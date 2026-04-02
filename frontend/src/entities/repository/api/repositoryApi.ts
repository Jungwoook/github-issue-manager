import { apiRequest } from '@/shared/api/client';

import type { Repository } from '@/entities/repository/model/types';

export function getRepositories() {
  return apiRequest<Repository[]>('/repositories');
}

export function refreshRepositories() {
  return apiRequest<Repository[]>('/repositories/refresh', {
    method: 'POST',
  });
}

export function getRepository(repositoryId: number | string) {
  return apiRequest<Repository>(`/repositories/${repositoryId}`);
}
