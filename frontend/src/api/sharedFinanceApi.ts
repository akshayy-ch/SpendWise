import { api } from "./client";
import type { ExpenseShare, Settlement, SettlementPage } from "../types/shared";

export const sharedFinanceApi = {
  getMyShares: async () => (await api.get<ExpenseShare[]>("/expense-shares/my")).data,
  getExpenseShares: async (expenseId: string) => (await api.get<ExpenseShare[]>("/expense-shares/expense/" + expenseId)).data,
  createSettlement: async (expenseShareId: string, payload: { amount: number; receiverId: string; categoryName: string; walletName: string }) =>
    (await api.post<Settlement>("/settlements/expense-shares/" + expenseShareId, payload)).data,
  getMySettlements: async (page = 0, size = 10, sort = "settledAt,desc") =>
    (await api.get<SettlementPage>("/settlements/my", { params: { page, size, sort } })).data,
  getSettlementsForShare: async (expenseShareId: string) =>
    (await api.get<Settlement[]>("/settlements/expense-shares/" + expenseShareId)).data,
  createGroup: async (name: string, description?: string) =>
    (await api.post("/groups/createGroup", { name, description })).data,
  archiveGroup: async (groupId: string) =>
    (await api.patch("/groups/" + groupId + "/archive")).data,
};
