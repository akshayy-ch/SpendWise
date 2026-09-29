export interface OpenGroup { groupId: string; groupName: string; openExpenseCount: number; }
export interface ExpenseShare { id: string; expenseId: string; expenseTitle: string; originalAmount: number; remainingAmount: number; status: string; groupId: string; percentage: number; }
export interface Settlement { id: string; expenseShareId: string; payerName: string; receiverName: string; categoryName: string; amount: number; settledAt: string; status: string; }
export interface SettlementPage { content: Settlement[]; page: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean; }
