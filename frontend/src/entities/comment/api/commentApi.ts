import { apiRequest } from '@/shared/api/client';

import type { Comment, CreateCommentPayload } from '@/entities/comment/model/types';

export function getComments(repositoryId: number | string, issueId: number | string) {
  return apiRequest<Comment[]>(`/repositories/${repositoryId}/issues/${issueId}/comments`);
}

export function refreshComments(repositoryId: number | string, issueId: number | string) {
  return apiRequest<Comment[]>(`/repositories/${repositoryId}/issues/${issueId}/comments/refresh`, {
    method: 'POST',
  });
}

export function createComment(
  repositoryId: number | string,
  issueId: number | string,
  payload: CreateCommentPayload,
) {
  return apiRequest<Comment>(`/repositories/${repositoryId}/issues/${issueId}/comments`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
