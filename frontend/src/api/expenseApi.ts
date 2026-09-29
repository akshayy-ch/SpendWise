import { api } from "./client";
import type { CreateExpenseRequest, ExpenseFilters, ExpensePage } from "../types/expense";

export const expenseApi = {
  getExpenses: async (filters: ExpenseFilters, page = 0, size = 10, sort = "expenseAt,desc") =>
    (await api.get<ExpensePage>("/expenses/getExpenses", { params: { ...filters, page, size, sort } })).data,
  createExpense: async (payload: CreateExpenseRequest) =>
    (await api.post("/expenses/createExpense", payload)).data,
  voidExpense: async (expenseId: string) =>
    (await api.patch("/expenses/" + expenseId + "/void")).data,
};
