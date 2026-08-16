import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { toast } from "sonner";

import * as authApi from "@/api/authApi";
import { setSessionExpiredHandler } from "@/lib/apiClient";
import {
  clearSelectedWorkspaceId,
  clearTokens,
  getAccessToken,
  setTokens,
} from "@/lib/authStorage";
import { getErrorMessage } from "@/lib/errorHandler";

interface AuthContextValue {
  isAuthenticated: boolean;
  isReady: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isReady, setIsReady] = useState(false);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // Restore session from storage after hydration.
  useEffect(() => {
    setIsAuthenticated(Boolean(getAccessToken()));
    setIsReady(true);
  }, []);

  const clearSession = useCallback(() => {
    clearTokens();
    clearSelectedWorkspaceId();
    setIsAuthenticated(false);
    queryClient.clear();
  }, [queryClient]);

  // Refresh failure -> hard sign-out.
  useEffect(() => {
    setSessionExpiredHandler(() => {
      clearSession();
      toast.error("Your session expired. Please sign in again.");
      void navigate({ to: "/login", replace: true });
    });
    return () => setSessionExpiredHandler(null);
  }, [clearSession, navigate]);

  const login = useCallback(
    async (email: string, password: string) => {
      const tokens = await authApi.login({ email, password });
      if (!tokens?.accessToken) throw new Error("Login response did not contain an access token.");
      setTokens(tokens.accessToken, tokens.refreshToken);
      setIsAuthenticated(true);
    },
    [],
  );

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch (error) {
      // Local state must be cleared regardless of backend failure.
      console.warn("Backend logout failed:", getErrorMessage(error));
    }
    await queryClient.cancelQueries();
    clearSession();
    void navigate({ to: "/login", replace: true });
  }, [clearSession, navigate, queryClient]);

  const value = useMemo(
    () => ({ isAuthenticated, isReady, login, logout }),
    [isAuthenticated, isReady, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}
