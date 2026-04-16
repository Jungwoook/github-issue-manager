import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type {
  GitHubTokenStatus,
  RegisterGitHubTokenPayload,
} from '@/entities/github/model/types';

export function getGitHubTokenStatus() {
  return apiRequest<GitHubTokenStatus>(withPlatform('/token/status', DEFAULT_PLATFORM));
}

export function registerGitHubToken(payload: RegisterGitHubTokenPayload) {
  return apiRequest(withPlatform('/token', DEFAULT_PLATFORM), {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function disconnectGitHubToken() {
  return apiRequest<void>(withPlatform('/token', DEFAULT_PLATFORM), {
    method: 'DELETE',
  });
}
