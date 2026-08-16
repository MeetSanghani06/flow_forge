import axios from "axios";

import type { ApiErrorResponse } from "@/types/api";

export class ApiError extends Error {
  status?: number | undefined;
  code?: string | undefined;
  details?: ApiErrorResponse | undefined;

  constructor(message: string, status?: number, code?: string, details?: ApiErrorResponse) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

function statusFallbackMessage(status?: number): string {
  switch (status) {
    case 400:
      return "Invalid request. Please check the submitted values.";
    case 401:
      return "Your session has expired. Please sign in again.";
    case 403:
      return "You do not have permission to perform this action.";
    case 404:
      return "The requested resource was not found.";
    case 409:
      return "Conflict: the resource was modified or already exists.";
    case 429:
      return "Execution rate limit exceeded. Please try again later.";
    case 500:
      return "The server encountered an error. Please try again.";
    default:
      return status
        ? `Request failed with status ${status}.`
        : "Unable to reach the FlowForge backend.";
  }
}

/** Centralized parser: turns any thrown value into a typed ApiError. */
export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;

  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const payload = error.response?.data as ApiErrorResponse | undefined;
    const first = payload?.errors?.[0];
    const message =
      first?.message ||
      payload?.message ||
      payload?.error ||
      (error.code === "ERR_NETWORK"
        ? "Cannot reach the FlowForge backend. Check that the API is running."
        : "") ||
      statusFallbackMessage(status);
    return new ApiError(message, status, first?.code, payload);
  }

  if (error instanceof Error) return new ApiError(error.message);
  return new ApiError("Unexpected error.");
}

export function getErrorMessage(error: unknown): string {
  return toApiError(error).message;
}
