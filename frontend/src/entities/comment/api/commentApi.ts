import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type { Comment, CreateCommentPayload } from '@/entities/comment/model/types';

export function getComments(repositoryId: number | string, issueId: number | string) {
  return apiRequest<Comment[]>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/comments`, DEFAULT_PLATFORM));
}

export function refreshComments(repositoryId: number | string, issueId: number | string) {
  return apiRequest<Comment[]>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/comments/refresh`, DEFAULT_PLATFORM), {
    method: 'POST',
  });
}

export function createComment(
  repositoryId: number | string,
  issueId: number | string,
  payload: CreateCommentPayload,
) {
  return apiRequest<Comment>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/comments`, DEFAULT_PLATFORM), {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
