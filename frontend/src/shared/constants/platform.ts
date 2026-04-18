export const DEFAULT_PLATFORM = 'github';
export const SUPPORTED_PLATFORMS = ['github', 'gitlab'] as const;
export type SupportedPlatform = (typeof SUPPORTED_PLATFORMS)[number];

export interface PlatformMetadata {
  label: string;
  tokenLabel: string;
  tokenPlaceholder: string;
  tokenHelp: string;
  defaultBaseUrl?: string;
  supportsCustomBaseUrl: boolean;
}

export const PLATFORM_METADATA: Record<SupportedPlatform, PlatformMetadata> = {
  github: {
    label: 'GitHub',
    tokenLabel: 'Personal Access Token',
    tokenPlaceholder: 'github_pat_...',
    tokenHelp: 'GitHub fine-grained PAT를 등록하면 저장소와 이슈를 불러올 수 있습니다.',
    supportsCustomBaseUrl: false,
  },
  gitlab: {
    label: 'GitLab',
    tokenLabel: 'Personal Access Token',
    tokenPlaceholder: 'glpat-...',
    tokenHelp: 'GitLab PAT를 등록하면 프로젝트와 이슈를 불러올 수 있습니다. 비워두면 GitLab.com을 사용합니다.',
    defaultBaseUrl: 'https://gitlab.com/api/v4',
    supportsCustomBaseUrl: true,
  },
};

export function normalizePlatformValue(platform?: string): SupportedPlatform {
  if (!platform) {
    return DEFAULT_PLATFORM;
  }

  return SUPPORTED_PLATFORMS.includes(platform as SupportedPlatform)
    ? (platform as SupportedPlatform)
    : DEFAULT_PLATFORM;
}

export function withPlatform(path: string, platform = DEFAULT_PLATFORM) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `/platforms/${platform}${normalizedPath}`;
}
