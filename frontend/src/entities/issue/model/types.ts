export type IssueStatus = 'OPEN' | 'CLOSED';
export type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface IssueSummary {
  githubIssueId: number;
  number: number;
  title: string;
  status: IssueStatus;
  priority: IssuePriority;
  authorLogin: string | null;
  createdAt: string;
  updatedAt: string;
  lastSyncedAt: string;
}

export interface IssueDetail extends IssueSummary {
  githubRepositoryId: number;
  body: string | null;
  closedAt: string | null;
}

export interface IssueFilter {
  keyword?: string;
  state?: IssueStatus;
}

export interface CreateIssuePayload {
  title: string;
  body?: string;
}

export interface UpdateIssuePayload {
  title?: string;
  body?: string;
  state?: IssueStatus;
}
