export type NotificationType="EXPENSE_SHARE_CREATED"|"SETTLEMENT_RECEIVED"|"GROUP_MEMBER_ADDED"|"BUDGET_EXCEEDED";
export interface Notification { id:string; type:NotificationType; title:string; message:string; referenceId:string|null; isRead:boolean; createdAt:string; }
export interface NotificationPage { content:Notification[]; page:number; size:number; totalElements:number; totalPages:number; first:boolean; last:boolean; }
