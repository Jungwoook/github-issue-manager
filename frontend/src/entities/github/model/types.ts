export interface GitHubTokenStatus {
  connected: boolean;
  githubLogin: string | null;
  tokenScopes: string | null;
  tokenVerifiedAt: string | null;
}

export interface RegisterGitHubTokenPayload {
  accessToken: string;
}
