import { useEffect,useState } from "react";
import { Bell, CheckCheck, CircleDollarSign, Users, WalletCards } from "lucide-react";
import { notificationApi } from "../../api/notificationApi";
import type { Notification } from "../../types/notification";
const dt=new Intl.DateTimeFormat("en-IN",{day:"2-digit",month:"short",year:"numeric",hour:"2-digit",minute:"2-digit"});
const icon=(type:Notification["type"])=>type==="BUDGET_EXCEEDED"?<WalletCards size={15}/>:type==="GROUP_MEMBER_ADDED"?<Users size={15}/>:<CircleDollarSign size={15}/>;
export default function NotificationsPage(){
 const [items,setItems]=useState<Notification[]>([]),[unread,setUnread]=useState(0),[filter,setFilter]=useState<"all"|"unread"|"read">("all"),[page,setPage]=useState(0),[pages,setPages]=useState(0),[loading,setLoading]=useState(true),[error,setError]=useState("");
 async function load(){setLoading(true);setError("");try{const [data,count]=await Promise.all([notificationApi.get(page,10,filter==="all"?undefined:filter==="unread"),notificationApi.unreadCount()]);const content=data.content;setItems(content);setPages(data.totalPages);setUnread(count);}catch(err:any){setError(err.response?.data?.message??"Couldn't load notifications.");}finally{setLoading(false);}}
 useEffect(()=>{load()},[page,filter]);
 async function read(id:string){try{await notificationApi.markRead(id);setItems(items.map(n=>n.id===id?{...n,isRead:true}:n));setUnread(Math.max(0,unread-1));}catch(err:any){setError(err.response?.data?.message??"Couldn't mark notification as read.");}}
 async function readAll(){try{await notificationApi.markAllRead();setItems(items.map(n=>({...n,isRead:true})));setUnread(0);}catch(err:any){setError(err.response?.data?.message??"Couldn't mark notifications as read.");}}
 return <div className="finance-page"><section className="page-intro"><div><span className="header-kicker">Updates</span><h2>Notifications.</h2><p>Important activity from your SpendWise account.</p></div>{unread>0&&<button className="button button-secondary" onClick={readAll}><CheckCheck size={14}/> Mark all read</button>}</section>
 <div className="notification-toolbar"><div className="notification-tabs">{(["all","unread","read"] as const).map(x=><button className={filter===x?"active":""} key={x} onClick={()=>{setPage(0);setFilter(x)}}>{x[0].toUpperCase()+x.slice(1)}{x==="unread"&&unread>0?<b>{unread}</b>:null}</button>)}</div></div>
 {error&&<div className="inline-error"><Bell size={15}/>{error}<button onClick={load}>Retry</button></div>}
 {loading?<div className="notification-list">{[1,2,3,4].map(i=><div className="notification-skeleton" key={i}/>)}</div>:items.length===0?<div className="notification-empty"><Bell size={22}/><b>You're all caught up.</b><span>No notifications match this filter.</span></div>:<div className="notification-list">{items.map(n=><article className={"notification-card "+(n.isRead?"read":"unread")} key={n.id}><span className="notification-icon">{icon(n.type)}</span><div><div className="notification-title"><b>{n.title}</b>{!n.isRead&&<i/>}</div><p>{n.message}</p><small>{dt.format(new Date(n.createdAt))}</small></div>{!n.isRead&&<button className="notification-read" onClick={()=>read(n.id)}>Mark read</button>}</article>)}</div>}
 {!loading&&pages>1&&<div className="pagination"><button disabled={page===0} onClick={()=>setPage(page-1)}>Previous</button><span>Page {page+1} of {pages}</span><button disabled={page>=pages-1} onClick={()=>setPage(page+1)}>Next</button></div>}
 </div>;
}
