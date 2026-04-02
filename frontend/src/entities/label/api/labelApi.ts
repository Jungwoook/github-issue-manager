import { apiRequest } from '@/shared/api/client';

import type { CreateLabelPayload, Label } from '@/entities/label/model/types';
import type { IssueDetail } from '@/entities/issue/model/types';

export function getLabels(repositoryId: number | string) {
  return apiRequest<Label[]>(`/repositories/${repositoryId}/labels`);
}

export function createLabel(repositoryId: number | string, payload: CreateLabelPayload) {
  return apiRequest<Label>(`/repositories/${repositoryId}/labels`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function attachLabelToIssue(
  repositoryId: number | string,
  issueId: number | string,
  labelId: number | string,
) {
  return apiRequest<IssueDetail>(`/repositories/${repositoryId}/issues/${issueId}/labels/${labelId}`, {
    method: 'POST',
  });
}

export function detachLabelFromIssue(
  repositoryId: number | string,
  issueId: number | string,
  labelId: number | string,
) {
  return apiRequest<void>(`/repositories/${repositoryId}/issues/${issueId}/labels/${labelId}`, {
    method: 'DELETE',
  });
}
