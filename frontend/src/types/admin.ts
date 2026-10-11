import type { Category } from "./category";
export type Role="USER"|"ADMIN";
export interface AdminUser { id:string; username:string; name:string; email:string; phoneNumber:string; role:Role; }
export interface AdminCategory extends Omit<Category, "icon"> { id:string; name:string; icon:string|null; }
