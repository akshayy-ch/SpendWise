import { api } from "./client";
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from "../types/auth";

export const authApi = {
  register: async (payload: RegisterRequest) =>
    (await api.post<RegisterResponse>("/auth/register", payload)).data,

  login: async (payload: LoginRequest) =>
    (await api.post<LoginResponse>("/auth/login", payload)).data,

  verifyEmail: async (token: string) =>
    (await api.get<string>("/auth/verify-email", { params: { token } })).data,

  resendVerification: async (email: string) =>
    (await api.post<string>("/auth/resend-verification", { email })).data,
};
