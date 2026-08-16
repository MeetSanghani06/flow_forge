import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from "axios";

import { clearTokens, getAccessToken, getRefreshToken, setTokens } from "@/lib/authStorage";
import { toApiError } from "@/lib/errorHandler";
import type { ApiResponse } from "@/types/api";

export const API_BASE_URL: string =
  (import.meta.env['VITE_API_BASE_URL'] as string | undefined) ?? "http://localhost:8080";

/** Raw axios instance without interceptors — used for the refresh call itself. */
export const rawClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

/** Unwraps `{ data, success, timestamp }` envelopes. */
export function unwrap<T>(response: AxiosResponse<ApiResponse<T> | T>): T {
  const body = response.data as ApiResponse<T> & { data?: T };
  if (body && typeof body === "object" && "data" in body && "success" in body) {
    return body.data as T;
  }
  return body as T;
}

/** Normalizes list payloads that may arrive as an array or a paged object. */
export function asArray<T>(value: unknown): T[] {
  if (Array.isArray(value)) return value as T[];
  if (value && typeof value === "object") {
    const record = value as Record<string, unknown>;
    for (const key of ["content", "items", "results", "data"]) {
      if (Array.isArray(record[key])) return record[key] as T[];
    }
  }
  return [];
}

// --- session-expired notification -------------------------------------------------

type SessionExpiredHandler = () => void;
let onSessionExpired: SessionExpiredHandler | null = null;

export function setSessionExpiredHandler(handler: SessionExpiredHandler | null) {
  onSessionExpired = handler;
}

// --- refresh queue ----------------------------------------------------------------

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean; _skipAuth?: boolean };

let isRefreshing = false;
let pendingQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

function flushQueue(error: unknown, token: string | null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (token) resolve(token);
    else reject(error);
  });
  pendingQueue = [];
}

/**
 * SINGLE PLACE TO ADJUST THE REFRESH CONTRACT.
 * Kept here (and mirrored in src/api/authApi.ts) so a backend contract change
 * only needs one edit.
 */
async function performRefresh(): Promise<string> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error("No refresh token available");

  const response = await rawClient.post("/api/v1/auth/refresh", { refreshToken });
  const tokens = unwrap<{ accessToken: string; refreshToken?: string }>(response);
  if (!tokens?.accessToken) throw new Error("Refresh response did not contain an access token");

  setTokens(tokens.accessToken, tokens.refreshToken ?? refreshToken);
  return tokens.accessToken;
}

apiClient.interceptors.request.use((config) => {
  const typed = config as RetriableConfig;
  if (!typed._skipAuth) {
    const token = getAccessToken();
    if (token) config.headers.set("Authorization", `Bearer ${token}`);
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (!axios.isAxiosError(error) || !error.config) {
      return Promise.reject(toApiError(error));
    }

    const config = error.config as RetriableConfig;
    const status = error.response?.status;

    // 429 must never be retried automatically.
    if (status !== 401 || config._retried || config._skipAuth || !getRefreshToken()) {
      return Promise.reject(toApiError(error));
    }

    config._retried = true;

    if (isRefreshing) {
      // Wait for the in-flight refresh instead of firing another one.
      return new Promise((resolve, reject) => {
        pendingQueue.push({
          resolve: (token: string) => {
            config.headers.set("Authorization", `Bearer ${token}`);
            resolve(apiClient(config));
          },
          reject: (queueError) => reject(toApiError(queueError)),
        });
      });
    }

    isRefreshing = true;
    try {
      const token = await performRefresh();
      flushQueue(null, token);
      config.headers.set("Authorization", `Bearer ${token}`);
      return await apiClient(config);
    } catch (refreshError) {
      flushQueue(refreshError, null);
      clearTokens();
      onSessionExpired?.();
      return Promise.reject(toApiError(error));
    } finally {
      isRefreshing = false;
    }
  },
);
