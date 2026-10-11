import { api } from "./client";
import type { AdminUser, Role } from "../types/admin";
import type { Category } from "../types/category";
export const adminApi={
 getUsers:async()=> (await api.get<AdminUser[]>("/admin/users")).data,
 updateRole:async(id:string,role:Role)=> (await api.patch<AdminUser>("/admin/users/"+id+"/role",{role})).data,
 createCategory:async(name:string,icon:string)=> (await api.post<Category>("/admin/categories",{name,icon:icon||undefined})).data,
 updateCategory:async(id:string,name:string,icon:string)=> (await api.patch<Category>("/admin/categories/"+id,{name,icon:icon||undefined})).data,
 deleteCategory:async(id:string)=> (await api.delete<Category>("/admin/categories/"+id)).data,
};
