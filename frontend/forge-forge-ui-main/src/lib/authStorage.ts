// Single source of truth for token persistence. Never touch localStorage
// for tokens anywhere else in the app.

const ACCESS_TOKEN_KEY = "flowforge.accessToken";
const REFRESH_TOKEN_KEY = "flowforge.refreshToken";

const isBrowser = () => typeof window !== "undefined";

export function getAccessToken(): string | null {
  if (!isBrowser()) return null;
  return window.localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string): void {
  if (!isBrowser()) return;
  window.localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function getRefreshToken(): string | null {
  if (!isBrowser()) return null;
  return window.localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setRefreshToken(token: string): void {
  if (!isBrowser()) return;
  window.localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function setTokens(accessToken: string, refreshToken?: string | null): void {
  setAccessToken(accessToken);
  if (refreshToken) setRefreshToken(refreshToken);
}

export function clearTokens(): void {
  if (!isBrowser()) return;
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function hasSession(): boolean {
  return Boolean(getAccessToken());
}

const SELECTED_WORKSPACE_KEY = "flowforge.selectedWorkspaceId";

export function getSelectedWorkspaceId(): string | null {
  if (!isBrowser()) return null;
  return window.localStorage.getItem(SELECTED_WORKSPACE_KEY);
}

export function setSelectedWorkspaceId(id: string): void {
  if (!isBrowser()) return;
  window.localStorage.setItem(SELECTED_WORKSPACE_KEY, id);
}

export function clearSelectedWorkspaceId(): void {
  if (!isBrowser()) return;
  window.localStorage.removeItem(SELECTED_WORKSPACE_KEY);
}
