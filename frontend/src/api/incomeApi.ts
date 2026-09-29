import { api } from "./client";
import type { CreateIncomeRequest, IncomeFilters, IncomePage } from "../types/income";

export const incomeApi = {
  getIncome: async (filters: IncomeFilters, page = 0, size = 10, sort = "incomeAt,desc") =>
    (await api.get<IncomePage>("/incomes/getIncome", { params: { ...filters, page, size, sort } })).data,
  createIncome: async (payload: CreateIncomeRequest) =>
    (await api.post("/incomes/createIncome", payload)).data,
};
