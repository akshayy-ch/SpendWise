import { api } from "./client";
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from "../types/auth";

export const authApi = {
  register: async (payload: RegisterRequest) =>
    (await api.post<RegisterResponse>("/v1/auth/register", payload)).data,

  login: async (payload: LoginRequest) =>
    (await api.post<LoginResponse>("/v1/auth/login", payload)).data,

  verifyEmail: async (token: string) =>
    (await api.get<string>("/v1/auth/verify-email", { params: { token } })).data,

  resendVerification: async (email: string) =>
    (await api.post<string>("/v1/auth/resend-verification", { email })).data,
};
