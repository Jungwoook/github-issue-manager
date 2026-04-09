import type { ApiErrorResponse } from '@/shared/types/api';

const DEFAULT_API_BASE_URL = '/api';

function normalizeBaseUrl(baseUrl?: string) {
  const trimmed = baseUrl?.trim();

  if (!trimmed) {
    return DEFAULT_API_BASE_URL;
  }

  return trimmed.endsWith('/') ? trimmed.slice(0, -1) : trimmed;
}

const API_BASE_URL = normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL);

export class ApiError extends Error {
  status: number;
  response?: ApiErrorResponse;

  constructor(status: number, response?: ApiErrorResponse) {
    super(response?.message ?? 'API request failed');
    this.name = 'ApiError';
    this.status = status;
    this.response = response;
  }
}

interface RequestOptions extends RequestInit {
  query?: object;
}

function createUrl(path: string, query?: RequestOptions['query']) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const url = new URL(`${API_BASE_URL}${normalizedPath}`, window.location.origin);

  if (query) {
    Object.entries(query as Record<string, unknown>).forEach(([key, value]) => {
      if (
        value === undefined ||
        value === null ||
        value === '' ||
        typeof value === 'object'
      ) {
        return;
      }

      url.searchParams.set(key, String(value));
    });
  }

  return url.toString();
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { query, headers, body, ...rest } = options;
  const response = await fetch(createUrl(path, query), {
    ...rest,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    body,
  });

  if (!response.ok) {
    let errorBody: ApiErrorResponse | undefined;

    try {
      errorBody = (await response.json()) as ApiErrorResponse;
    } catch {
      errorBody = undefined;
    }

    throw new ApiError(response.status, errorBody);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
