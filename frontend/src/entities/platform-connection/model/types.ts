export interface PlatformTokenStatus {
  platform: string;
  connected: boolean;
  accountLogin: string | null;
  tokenScopes: string | null;
  tokenVerifiedAt: string | null;
}

export interface RegisterPlatformTokenPayload {
  accessToken: string;
}
