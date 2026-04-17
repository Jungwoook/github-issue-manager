import { apiRequest } from '@/shared/api/client';
import { DEFAULT_PLATFORM, withPlatform } from '@/shared/constants/platform';

import type {
  PlatformTokenStatus,
  RegisterPlatformTokenPayload,
} from '@/entities/platform-connection/model/types';

export function getPlatformTokenStatus(platform = DEFAULT_PLATFORM) {
  return apiRequest<PlatformTokenStatus>(withPlatform('/token/status', platform));
}

export function registerPlatformToken(
  payload: RegisterPlatformTokenPayload,
  platform = DEFAULT_PLATFORM,
) {
  return apiRequest(withPlatform('/token', platform), {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function disconnectPlatformToken(platform = DEFAULT_PLATFORM) {
  return apiRequest<void>(withPlatform('/token', platform), {
    method: 'DELETE',
  });
}
