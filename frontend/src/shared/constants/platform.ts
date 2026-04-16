export const DEFAULT_PLATFORM = 'github';

export function withPlatform(path: string, platform = DEFAULT_PLATFORM) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `/platforms/${platform}${normalizedPath}`;
}
