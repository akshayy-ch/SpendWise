import { api } from "./client";
import type { NotificationPage } from "../types/notification";
export const notificationApi={
 get:async(page=0,size=10,unread?:boolean)=>(await api.get<NotificationPage>("/notifications",{params:{page,size,sort:"createdAt,desc",...(unread===undefined?{}:{unread})}})).data,
 unreadCount:async()=>(await api.get<number>("/notifications/unread-count")).data,
 markRead:async(id:string)=>api.patch("/notifications/"+id+"/read"),
 markAllRead:async()=>api.patch("/notifications/read-all"),
};
