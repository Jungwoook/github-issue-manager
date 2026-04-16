import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type { Repository } from '@/entities/repository/model/types';

export function getRepositories() {
  return apiRequest<Repository[]>(withPlatform('/repositories', DEFAULT_PLATFORM));
}

export function refreshRepositories() {
  return apiRequest<Repository[]>(withPlatform('/repositories/refresh', DEFAULT_PLATFORM), {
    method: 'POST',
  });
}

export function getRepository(repositoryId: number | string) {
  return apiRequest<Repository>(withPlatform(`/repositories/${repositoryId}`, DEFAULT_PLATFORM));
}
