import { isAxiosError } from "axios";

interface ApiErrorBody {
  message?: unknown;
  detail?: unknown;
  errors?: Record<string, string>;
}

export const getApiErrorStatus = (error: unknown): number | null =>
  isAxiosError(error) ? error.response?.status ?? null : null;

export const getApiErrorMessage = (error: unknown, fallback: string): string => {
  if (!isAxiosError(error)) return fallback;

  const body = error.response?.data as ApiErrorBody | undefined;
  if (body?.errors && Object.keys(body.errors).length > 0) {
    return Object.values(body.errors).join(" ");
  }

  const message = body?.message ?? body?.detail;
  return typeof message === "string" && message.trim() ? message : fallback;
};
