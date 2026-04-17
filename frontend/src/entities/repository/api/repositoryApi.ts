import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type { Repository } from '@/entities/repository/model/types';

export function getRepositories(platform = DEFAULT_PLATFORM) {
  return apiRequest<Repository[]>(withPlatform('/repositories', platform));
}

export function refreshRepositories(platform = DEFAULT_PLATFORM) {
  return apiRequest<Repository[]>(withPlatform('/repositories/refresh', platform), {
    method: 'POST',
  });
}

export function getRepository(repositoryId: number | string, platform = DEFAULT_PLATFORM) {
  return apiRequest<Repository>(withPlatform(`/repositories/${repositoryId}`, platform));
}
