import { apiRequest } from '@/shared/api/client';

import type {
  CreateIssuePayload,
  IssueDetail,
  IssueFilter,
  IssueSummary,
  IssueStatus,
  UpdateIssuePayload,
} from '@/entities/issue/model/types';

interface IssueSummaryResponse {
  githubIssueId: number;
  number: number;
  title: string;
  state: IssueStatus;
  authorLogin: string | null;
  createdAt: string;
  updatedAt: string;
  lastSyncedAt: string;
}

interface IssueDetailResponse extends IssueSummaryResponse {
  githubRepositoryId: number;
  body: string | null;
  closedAt: string | null;
}

function mapIssueSummary(issue: IssueSummaryResponse): IssueSummary {
  return {
    githubIssueId: issue.githubIssueId,
    number: issue.number,
    title: issue.title,
    status: issue.state,
    priority: issue.state === 'OPEN' ? 'MEDIUM' : 'LOW',
    authorLogin: issue.authorLogin,
    createdAt: issue.createdAt,
    updatedAt: issue.updatedAt,
    lastSyncedAt: issue.lastSyncedAt,
  };
}

function mapIssueDetail(issue: IssueDetailResponse): IssueDetail {
  return {
    ...mapIssueSummary(issue),
    githubRepositoryId: issue.githubRepositoryId,
    body: issue.body,
    closedAt: issue.closedAt,
  };
}

export async function getIssues(repositoryId: number | string, filters: IssueFilter = {}) {
  const issues = await apiRequest<IssueSummaryResponse[]>(`/repositories/${repositoryId}/issues`, {
    query: filters,
  });

  return issues.map(mapIssueSummary);
}

export async function refreshIssues(repositoryId: number | string) {
  const issues = await apiRequest<IssueSummaryResponse[]>(`/repositories/${repositoryId}/issues/refresh`, {
    method: 'POST',
  });

  return issues.map(mapIssueSummary);
}

export async function getIssueDetail(repositoryId: number | string, issueId: number | string) {
  const issue = await apiRequest<IssueDetailResponse>(`/repositories/${repositoryId}/issues/${issueId}`);
  return mapIssueDetail(issue);
}

export async function createIssue(repositoryId: number | string, payload: CreateIssuePayload) {
  const issue = await apiRequest<IssueDetailResponse>(`/repositories/${repositoryId}/issues`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  return mapIssueDetail(issue);
}

export async function updateIssue(
  repositoryId: number | string,
  issueId: number | string,
  payload: UpdateIssuePayload,
) {
  const issue = await apiRequest<IssueDetailResponse>(`/repositories/${repositoryId}/issues/${issueId}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });

  return mapIssueDetail(issue);
}

export function deleteIssue(repositoryId: number | string, issueId: number | string) {
  return apiRequest<void>(`/repositories/${repositoryId}/issues/${issueId}`, {
    method: 'DELETE',
  });
}
