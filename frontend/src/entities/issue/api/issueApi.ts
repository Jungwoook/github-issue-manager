import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type {
  CreateIssuePayload,
  IssueDetail,
  IssueFilter,
  IssueSummary,
  IssueState,
  UpdateIssuePayload,
} from '@/entities/issue/model/types';

interface IssueSummaryResponse {
  platform: string;
  issueId: string;
  numberOrKey: string;
  title: string;
  state: IssueState;
  authorLogin: string | null;
  createdAt: string;
  updatedAt: string;
  lastSyncedAt: string;
}

interface IssueDetailResponse extends IssueSummaryResponse {
  repositoryId: string;
  body: string | null;
  closedAt: string | null;
}

function mapIssueSummary(issue: IssueSummaryResponse): IssueSummary {
  return {
    platform: issue.platform,
    issueId: issue.issueId,
    numberOrKey: issue.numberOrKey,
    title: issue.title,
    state: issue.state,
    authorLogin: issue.authorLogin,
    createdAt: issue.createdAt,
    updatedAt: issue.updatedAt,
    lastSyncedAt: issue.lastSyncedAt,
  };
}

function mapIssueDetail(issue: IssueDetailResponse): IssueDetail {
  return {
    ...mapIssueSummary(issue),
    repositoryId: issue.repositoryId,
    body: issue.body,
    closedAt: issue.closedAt,
  };
}

export async function getIssues(repositoryId: number | string, filters: IssueFilter = {}, platform = DEFAULT_PLATFORM) {
  const issues = await apiRequest<IssueSummaryResponse[]>(
    withPlatform(`/repositories/${repositoryId}/issues`, platform),
    { query: filters },
  );

  return issues.map(mapIssueSummary);
}

export async function refreshIssues(repositoryId: number | string, platform = DEFAULT_PLATFORM) {
  const issues = await apiRequest<IssueSummaryResponse[]>(
    withPlatform(`/repositories/${repositoryId}/issues/refresh`, platform),
    { method: 'POST' },
  );

  return issues.map(mapIssueSummary);
}

export async function getIssueDetail(repositoryId: number | string, issueId: number | string, platform = DEFAULT_PLATFORM) {
  const issue = await apiRequest<IssueDetailResponse>(
    withPlatform(`/repositories/${repositoryId}/issues/${issueId}`, platform),
  );
  return mapIssueDetail(issue);
}

export async function createIssue(repositoryId: number | string, payload: CreateIssuePayload, platform = DEFAULT_PLATFORM) {
  const issue = await apiRequest<IssueDetailResponse>(withPlatform(`/repositories/${repositoryId}/issues`, platform), {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  return mapIssueDetail(issue);
}

export async function updateIssue(
  repositoryId: number | string,
  issueId: number | string,
  payload: UpdateIssuePayload,
  platform = DEFAULT_PLATFORM,
) {
  const issue = await apiRequest<IssueDetailResponse>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}`, platform), {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });

  return mapIssueDetail(issue);
}

export function deleteIssue(repositoryId: number | string, issueId: number | string, platform = DEFAULT_PLATFORM) {
  return apiRequest<void>(withPlatform(`/repositories/${repositoryId}/issues/${issueId}`, platform), {
    method: 'DELETE',
  });
}
