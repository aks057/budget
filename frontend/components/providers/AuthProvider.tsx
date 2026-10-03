"use client";

import { clearSession, onAuthEvent, refreshAccessToken, setAccessToken } from "@/lib/api/client";
import * as api from "@/lib/api/endpoints";
import type { UserDto } from "@/lib/api/types";
import { useQueryClient } from "@tanstack/react-query";
import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

export type AuthStatus = "loading" | "authenticated" | "anonymous";

interface AuthContextValue {
  status: AuthStatus;
  user: UserDto | null;
  login: (email: string, password: string) => Promise<void>;
  register: (fullName: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  loginWithGoogle: () => void;
  /** Re-read the session after an OAuth redirect, or replace the cached user after a profile update. */
  reloadSession: () => Promise<boolean>;
  setUser: (user: UserDto) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

const GOOGLE_AUTHORIZATION_PATH = "/oauth2/authorization/google";

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<AuthStatus>("loading");
  const [user, setUser] = useState<UserDto | null>(null);

  const becomeAnonymous = useCallback(() => {
    setUser(null);
    setStatus("anonymous");
    queryClient.clear();
  }, [queryClient]);

  /** Uses the httpOnly refresh cookie: survives reloads without storing any token in the browser. */
  const reloadSession = useCallback(async () => {
    if (!(await refreshAccessToken())) {
      becomeAnonymous();
      return false;
    }
    try {
      setUser(await api.getMe());
      setStatus("authenticated");
      return true;
    } catch {
      becomeAnonymous();
      return false;
    }
  }, [becomeAnonymous]);

  useEffect(() => {
    void reloadSession();
  }, [reloadSession]);

  // Logout / session expiry in this or another tab; login in another tab.
  useEffect(
    () =>
      onAuthEvent((event) => {
        if (event.type === "logout" || event.type === "expired") {
          becomeAnonymous();
        } else if (event.type === "token" && status === "anonymous") {
          void reloadSession();
        }
      }),
    [becomeAnonymous, reloadSession, status],
  );

  const login = useCallback(async (email: string, password: string) => {
    const response = await api.login(email, password);
    setAccessToken(response.accessToken);
    setUser(response.user);
    setStatus("authenticated");
  }, []);

  const register = useCallback(async (fullName: string, email: string, password: string) => {
    const response = await api.register(fullName, email, password);
    setAccessToken(response.accessToken);
    setUser(response.user);
    setStatus("authenticated");
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.logout();
    } finally {
      clearSession();
    }
  }, []);

  const loginWithGoogle = useCallback(() => {
    window.location.href = GOOGLE_AUTHORIZATION_PATH;
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ status, user, login, register, logout, loginWithGoogle, reloadSession, setUser }),
    [status, user, login, register, logout, loginWithGoogle, reloadSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside <AuthProvider>");
  }
  return context;
}

/** For screens rendered only inside <RequireAuth>: the user is guaranteed to be loaded. */
export function useCurrentUser(): UserDto {
  const { user } = useAuth();
  if (!user) {
    throw new Error("useCurrentUser called outside an authenticated route");
  }
  return user;
}
