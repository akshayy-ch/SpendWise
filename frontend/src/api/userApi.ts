import { api } from "./client";
import type { UserProfile, UserSearchResult } from "../types/user";

export const userApi = {
  getMe: async () => (await api.get<UserProfile>("/users/me")).data,
  search: async (query: string) => (await api.get<UserSearchResult[]>("/users/search", { params: { query } })).data,
};