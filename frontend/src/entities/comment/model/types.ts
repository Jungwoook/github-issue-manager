export interface Comment {
  githubCommentId: number;
  authorLogin: string;
  body: string;
  createdAt: string;
  updatedAt: string;
  lastSyncedAt: string;
}

export interface CreateCommentPayload {
  body: string;
}
