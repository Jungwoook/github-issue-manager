export const queryKeys = {
  platformTokenStatus: (platform: string) => ['platform-token-status', platform] as const,
  repositories: (platform: string) => ['repositories', platform] as const,
  repository: (platform: string, repositoryId: number | string) => ['repository', platform, repositoryId] as const,
  issuesRoot: (platform: string, repositoryId: number | string) => ['issues', platform, repositoryId] as const,
  issues: (platform: string, repositoryId: number | string, filters?: unknown) =>
    ['issues', platform, repositoryId, filters ?? {}] as const,
  issue: (platform: string, repositoryId: number | string, issueId: number | string) =>
    ['issue', platform, repositoryId, issueId] as const,
  users: (filters?: unknown) => ['users', filters ?? {}] as const,
  labels: (platform: string, repositoryId: number | string) => ['labels', platform, repositoryId] as const,
  comments: (platform: string, repositoryId: number | string, issueId: number | string) =>
    ['comments', platform, repositoryId, issueId] as const,
};
