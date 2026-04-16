export interface Comment {
  platform: string;
  commentId: string;
  authorLogin: string;
  body: string;
  createdAt: string;
  updatedAt: string;
  lastSyncedAt: string;
}

export interface CreateCommentPayload {
  body: string;
}
