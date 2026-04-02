import { ApiError } from '@/shared/api/client';
import type { ApiErrorResponse } from '@/shared/types/api';

const errorMessages: Record<string, string> = {
  DUPLICATE_USER_USERNAME: 'That username is already in use.',
  DUPLICATE_USER_EMAIL: 'That email address is already in use.',
  USER_DELETE_CONFLICT: 'This user is still assigned to an issue or comment and cannot be deleted.',
  DUPLICATE_LABEL_NAME: 'A label with the same name already exists in this repository.',
  LABEL_ALREADY_ATTACHED: 'That label is already attached to the issue.',
};

function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  return Boolean(
    value &&
      typeof value === 'object' &&
      'code' in value &&
      'message' in value &&
      typeof (value as { code?: unknown }).code === 'string' &&
      typeof (value as { message?: unknown }).message === 'string',
  );
}

export function getErrorMessage(error?: unknown) {
  if (error instanceof ApiError) {
    return getErrorMessage(error.response);
  }

  if (!error) {
    return 'An unexpected error occurred.';
  }

  if (isApiErrorResponse(error)) {
    return errorMessages[error.code] ?? error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'An unexpected error occurred.';
}
