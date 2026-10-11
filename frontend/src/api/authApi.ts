import { api } from "./client";
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from "../types/auth";

export const authApi = {
  register: async (payload: RegisterRequest) =>
    (await api.post<RegisterResponse>("/v1/auth/register", payload)).data,

  login: async (payload: LoginRequest) =>
    (await api.post<LoginResponse>("/v1/auth/login", payload)).data,
};
