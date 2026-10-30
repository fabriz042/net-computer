import { isAxiosError } from "axios";

interface ApiErrorBody {
  detail?: unknown;
  message?: unknown;
  errors?: Record<string, string>;
}

export const getApiErrorMessage = (
  error: unknown,
  fallback: string,
  conflictFallback: string = fallback
): string => {
  if (!isAxiosError(error)) return fallback;

  const body = error.response?.data as ApiErrorBody | undefined;

  if (body?.errors && Object.keys(body.errors).length > 0) {
    return Object.values(body.errors).join(" ");
  }

  const serverMessage = body?.detail ?? body?.message;
  if (typeof serverMessage === "string" && serverMessage.trim()) {
    return serverMessage;
  }

  return error.response?.status === 409 ? conflictFallback : fallback;
};
