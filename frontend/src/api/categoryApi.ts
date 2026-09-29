import { api } from "./client";
import type { Category } from "../types/category";

export const categoryApi = {
  getCategories: async (filter: string) => (await api.get<Category[]>("/categories/getCategories", { params: { filter } })).data,
  createCategory: async (name: string, icon?: string) => (await api.post<Category>("/categories/createCategory", { name, icon })).data,
};
