// src/lib/httpError.ts

export interface NormalizedHttpError {
  status?: number;
  title: string;
  description: string;
}

export function normalizeHttpError(error: any): NormalizedHttpError {
  const status: number | undefined = error?.response?.status;

  // Try to grab a meaningful message from the backend
  const data = error?.response?.data;
  const backendMessage =
    data?.message ||
    data?.detail ||
    (Array.isArray(data?.detail) && data.detail[0]?.msg) ||
    data?.error ||
    error?.message ||
    "Unexpected error occurred";

  let title = "Request failed";

  if (status === 400) title = "Bad request";
  else if (status === 401) title = "Unauthorized";
  else if (status === 403) title = "Forbidden";
  else if (status === 404) title = "Not found";
  else if (status && status >= 500) title = "Server error";

  return {
    status,
    title,
    description: backendMessage,
  };
}
