export const queryKeys = {
  githubTokenStatus: ['github-token-status'] as const,
  repositories: ['repositories'] as const,
  repository: (repositoryId: number | string) => ['repository', repositoryId] as const,
  issuesRoot: (repositoryId: number | string) => ['issues', repositoryId] as const,
  issues: (repositoryId: number | string, filters?: unknown) =>
    ['issues', repositoryId, filters ?? {}] as const,
  issue: (repositoryId: number | string, issueId: number | string) =>
    ['issue', repositoryId, issueId] as const,
  users: (filters?: unknown) => ['users', filters ?? {}] as const,
  labels: (repositoryId: number | string) => ['labels', repositoryId] as const,
  comments: (repositoryId: number | string, issueId: number | string) =>
    ['comments', repositoryId, issueId] as const,
};
