import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { authApi } from "../api/authApi";
import type { LoginRequest, RegisterRequest, RegisterResponse } from "../types/auth";

interface AuthContextValue {
  token: string | null;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<RegisterResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("spendwise_token"));

  useEffect(() => {
    const handleUnauthorized = () => setToken(null);
    window.addEventListener("spendwise:unauthorized", handleUnauthorized);
    return () => window.removeEventListener("spendwise:unauthorized", handleUnauthorized);
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    token,
    isAuthenticated: Boolean(token),
    login: async (payload) => {
      const response = await authApi.login(payload);
      localStorage.setItem("spendwise_token", response.authToken);
      setToken(response.authToken);
    },
    register: authApi.register,
    logout: () => {
      localStorage.removeItem("spendwise_token");
      setToken(null);
    },
  }), [token]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}
