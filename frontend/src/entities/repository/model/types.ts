export interface Repository {
  platform: string;
  repositoryId: string;
  ownerKey: string;
  name: string;
  fullName: string;
  description: string | null;
  webUrl: string;
  private: boolean;
  lastSyncedAt: string;
}
