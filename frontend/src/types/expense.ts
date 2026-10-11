import type { ExpenseStatus } from "./expenseStatus";

export interface Expense {
  id: string;
  title: string;
  amount: number;
  categoryName: string;
  walletName: string;
  status: ExpenseStatus;
  expenseAt: string;
}
export interface ExpensePage {
  content: Expense[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
export interface ExpenseFilters {
  categoryName?: string;
  walletName?: string;
  status?: ExpenseStatus;
  from?: string;
  to?: string;
  search?: string;
}
export interface CreateExpenseRequest {
  title: string;
  description?: string;
  amount: number;
  expenseAt: string;
  walletName: string;
  categoryName: string;
  status: string;
}