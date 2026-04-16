import { DEFAULT_PLATFORM } from '@/shared/constants/platform';

export function normalizePlatform(platform?: string) {
  return platform || DEFAULT_PLATFORM;
}

export function platformSettingsPath(platform?: string) {
  return `/settings/platforms/${normalizePlatform(platform)}`;
}

export function repositoriesPath(platform?: string) {
  return `/platforms/${normalizePlatform(platform)}/repositories`;
}

export function repositoryIssuesPath(repositoryId: string, platform?: string) {
  return `${repositoriesPath(platform)}/${repositoryId}/issues`;
}

export function repositoryIssueNewPath(repositoryId: string, platform?: string) {
  return `${repositoryIssuesPath(repositoryId, platform)}/new`;
}

export function repositoryIssuePath(repositoryId: string, issueId: string, platform?: string) {
  return `${repositoryIssuesPath(repositoryId, platform)}/${issueId}`;
}

export function repositoryIssueEditPath(repositoryId: string, issueId: string, platform?: string) {
  return `${repositoryIssuePath(repositoryId, issueId, platform)}/edit`;
}

export function repositoryLabelsPath(repositoryId: string, platform?: string) {
  return `${repositoriesPath(platform)}/${repositoryId}/labels`;
}
