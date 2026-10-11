import type { Expense } from "./expense";
import type { Income } from "./income";

export interface CategorySpending { budgetId:string; categoryName:string; spent:number; budget:number; }
export interface OpenGroup { groupId:string; groupName:string; openExpenseCount:number; }
export interface RecentActivity { id:string; type:"EXPENSE"|"INCOME"|"SETTLEMENT"; title:string; amount:number; occurredAt:string; }
export interface DashboardData {
  spentThisMonth:number;
  budgetRemaining:number|null;
  youOwe:number;
  youAreOwed:number;
  spendingByCategory:CategorySpending[];
  openGroups:OpenGroup[];
  recentActivity:RecentActivity[];
  latestExpenses: Expense[];
  latestIncome: Income[];
}