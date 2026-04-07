import { apiRequest } from '@/shared/api/client';

import type {
  GitHubTokenStatus,
  RegisterGitHubTokenPayload,
} from '@/entities/github/model/types';

export function getGitHubTokenStatus() {
  return apiRequest<GitHubTokenStatus>('/github/token/status');
}

export function registerGitHubToken(payload: RegisterGitHubTokenPayload) {
  return apiRequest('/github/token', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function disconnectGitHubToken() {
  return apiRequest<void>('/github/token', {
    method: 'DELETE',
  });
}
