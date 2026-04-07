export interface Repository {
  githubRepositoryId: number;
  ownerLogin: string;
  name: string;
  fullName: string;
  description: string | null;
  htmlUrl: string;
  private: boolean;
  lastSyncedAt: string;
}
