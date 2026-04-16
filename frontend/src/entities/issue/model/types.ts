export type IssueState = 'OPEN' | 'CLOSED';
export type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface IssueSummary {
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

export interface IssueDetail extends IssueSummary {
  repositoryId: string;
  body: string | null;
  closedAt: string | null;
}

export interface IssueFilter {
  keyword?: string;
  state?: IssueState;
}

export interface CreateIssuePayload {
  title: string;
  body?: string;
}

export interface UpdateIssuePayload {
  title?: string;
  body?: string;
  state?: IssueState;
}
