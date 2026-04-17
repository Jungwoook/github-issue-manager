import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type { CreateLabelPayload, Label } from '@/entities/label/model/types';
import type { IssueDetail } from '@/entities/issue/model/types';

export function getLabels(repositoryId: number | string, platform = DEFAULT_PLATFORM) {
  return apiRequest<Label[]>(withPlatform(`/repositories/${repositoryId}/labels`, platform));
}

export function createLabel(repositoryId: number | string, payload: CreateLabelPayload, platform = DEFAULT_PLATFORM) {
  return apiRequest<Label>(withPlatform(`/repositories/${repositoryId}/labels`, platform), {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function attachLabelToIssue(
  repositoryId: number | string,
  issueId: number | string,
  labelId: number | string,
  platform = DEFAULT_PLATFORM,
) {
  return apiRequest<IssueDetail>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/labels/${labelId}`, platform), {
    method: 'POST',
  });
}

export function detachLabelFromIssue(
  repositoryId: number | string,
  issueId: number | string,
  labelId: number | string,
  platform = DEFAULT_PLATFORM,
) {
  return apiRequest<void>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}/labels/${labelId}`, platform), {
    method: 'DELETE',
  });
}
