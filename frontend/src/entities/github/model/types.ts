export interface GitHubTokenStatus {
  platform: string;
  connected: boolean;
  accountLogin: string | null;
  tokenScopes: string | null;
  tokenVerifiedAt: string | null;
}

export interface RegisterGitHubTokenPayload {
  accessToken: string;
}
