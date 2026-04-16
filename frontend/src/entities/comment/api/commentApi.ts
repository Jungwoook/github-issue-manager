import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type { Comment, CreateCommentPayload } from '@/entities/comment/model/types';

export function getComments(repositoryId: number | string, issueId: number | string, platform = DEFAULT_PLATFORM) {
  return apiRequest<Comment[]>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/comments`, platform));
}

export function refreshComments(repositoryId: number | string, issueId: number | string, platform = DEFAULT_PLATFORM) {
  return apiRequest<Comment[]>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/comments/refresh`, platform), {
    method: 'POST',
  });
}

export function createComment(
  repositoryId: number | string,
  issueId: number | string,
  payload: CreateCommentPayload,
  platform = DEFAULT_PLATFORM,
) {
  return apiRequest<Comment>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/comments`, platform), {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
