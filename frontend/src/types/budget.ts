export interface CategoryBudgetItem { categoryName: string; limit: number; }
export interface Budget { id: string; overallBudget: number | null; categoryBudgets: CategoryBudgetItem[]; startDate: string; endDate: string; }
export interface CreateBudgetRequest { overallBudget?: number; categoryBudgets: CategoryBudgetItem[]; startDate: string; endDate: string; }
