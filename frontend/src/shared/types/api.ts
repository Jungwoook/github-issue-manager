export interface ApiErrorField {
  field: string;
  reason: string;
}

export interface ApiErrorResponse {
  code: string;
  message: string;
  timestamp: string;
  errors?: ApiErrorField[];
}
