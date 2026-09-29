import { api } from "./client";
import type { CategorySpending, OpenGroup, RecentActivity } from "../types/analytics";

export const analyticsApi = {
 getSpentThisMonth: async()=> (await api.get<number>("/analytics/spent-this-month")).data,
 getBudgetRemaining: async()=> (await api.get<number|null>("/analytics/budget-remaining")).data,
 getYouOwe: async()=> (await api.get<number>("/analytics/you-owe")).data,
 getYouAreOwed: async()=> (await api.get<number>("/analytics/you-are-owed")).data,
 getSpendingByCategory: async()=> (await api.get<CategorySpending[]>("/analytics/spending-by-category")).data,
 getOpenGroups: async()=> (await api.get<OpenGroup[]>("/analytics/open-groups")).data,
 getRecentActivity: async()=> (await api.get<RecentActivity[]>("/analytics/recent-activity")).data,
};
