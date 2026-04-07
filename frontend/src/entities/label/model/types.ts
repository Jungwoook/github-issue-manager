export interface Label {
  id: number;
  repositoryId: number;
  name: string;
  color: string;
}

export interface CreateLabelPayload {
  name: string;
  color: string;
}
