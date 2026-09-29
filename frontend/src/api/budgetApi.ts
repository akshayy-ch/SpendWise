import { api } from "./client";
import type { Budget, CreateBudgetRequest } from "../types/budget";

export const budgetApi = {
  getCurrent: async () => (await api.get<Budget>("/budgets/current")).data,
  create: async (payload: CreateBudgetRequest) => (await api.post<Budget>("/budgets", payload)).data,
  update: async (payload: Partial<CreateBudgetRequest>) => (await api.patch<Budget>("/budgets/current", payload)).data,
  remove: async () => api.delete("/budgets/current"),
};
