// ============================================================================
// AUTH CONTRACT — the ONLY place that knows the shape of auth requests and
// responses. Adjust here if the backend contract differs.
// ============================================================================
import { apiClient, rawClient, unwrap } from "@/lib/apiClient";
import type { AuthTokens, RegisteredUser } from "@/types/api";

export interface LoginRequest {
  email: string;
  password: string;
}

export async function login(payload: LoginRequest): Promise<AuthTokens> {
  const response = await rawClient.post("/api/v1/auth/login", payload);
  // Response envelope: { data: { accessToken, refreshToken, tokenType, expiresIn }, success }
  return unwrap<AuthTokens>(response);
}

export interface RegisterRequest {
  email: string;
  password: string;
}

/**
 * Registration returns only the created user ({ id, email }) — never tokens.
 * The caller must send the user to the login screen.
 */
export async function register(payload: RegisterRequest): Promise<RegisteredUser> {
  const response = await rawClient.post("/api/v1/auth/register", payload);
  return unwrap<RegisteredUser>(response);
}

export async function refresh(refreshToken: string): Promise<AuthTokens> {
  const response = await rawClient.post("/api/v1/auth/refresh", { refreshToken });
  return unwrap<AuthTokens>(response);
}

export async function logout(): Promise<void> {
  await apiClient.post("/api/v1/auth/logout", {});
}
