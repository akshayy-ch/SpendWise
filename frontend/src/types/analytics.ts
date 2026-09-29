export interface CategorySpending { budgetId:string; categoryName:string; spent:number; budget:number; }
export interface OpenGroup { groupId:string; groupName:string; openExpenseCount:number; }
export interface RecentActivity { id:string; type:"EXPENSE"|"INCOME"|"SETTLEMENT"; title:string; amount:number; occurredAt:string; }
