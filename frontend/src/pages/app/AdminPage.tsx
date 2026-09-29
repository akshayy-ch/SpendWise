import { useEffect,useState } from "react";
import { Shield, UserCog, Tags, Plus } from "lucide-react";
import { adminApi } from "../../api/adminApi";
import type { AdminUser, Role } from "../../types/admin";
import type { Category } from "../../types/category";
export default function AdminPage(){
 const [users,setUsers]=useState<AdminUser[]>([]),[categories,setCategories]=useState<Category[]>([]),[name,setName]=useState(""),[icon,setIcon]=useState(""),[loading,setLoading]=useState(true),[error,setError]=useState("");
 async function load(){setLoading(true);setError("");try{const [u,c]=await Promise.all([adminApi.getUsers(),(await import("../../api/categoryApi")).categoryApi.getCategories("SYSTEM")]);setUsers(u);setCategories(c);}catch(err:any){setError(err.response?.data?.message??"Couldn't load admin data.");}finally{setLoading(false);}}
 useEffect(()=>{load()},[]);
 async function role(id:string,role:Role){try{const updated=await adminApi.updateRole(id,role);setUsers(users.map(u=>u.id===id?updated:u));}catch(err:any){setError(err.response?.data?.message??"Couldn't update the user role.");}}
 async function create(e:React.FormEvent){e.preventDefault();if(!name.trim())return;try{const c=await adminApi.createCategory(name.trim(),icon.trim());setCategories([...categories,c]);setName("");setIcon("");}catch(err:any){setError(err.response?.data?.message??"Couldn't create the system category.");}}

 if(loading)return <div className="finance-page"><div className="expense-skeleton">{[1,2,3,4].map(i=><div key={i}/>)}</div></div>;
 return <div className="finance-page"><section className="page-intro"><div><span className="header-kicker">Administration</span><h2>Admin.</h2><p>Manage SpendWise users and system categories.</p></div><Shield size={20}/></section>{error&&<div className="inline-error"><Shield size={15}/>{error}<button onClick={load}>Retry</button></div>}
 <section className="admin-grid"><div className="admin-panel"><div className="panel-head"><div><h3>Users</h3><p>{users.length} registered users</p></div><UserCog size={18}/></div><div className="admin-users">{users.map(u=><div className="admin-user-row" key={u.id}><div><b>{u.name}</b><span>@{u.username} · {u.email}</span></div><select value={u.role} onChange={e=>role(u.id,e.target.value as Role)}><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></div>)}</div></div>
 <div className="admin-panel"><div className="panel-head"><div><h3>System categories</h3><p>Available across SpendWise</p></div><Tags size={18}/></div><form className="admin-category-form" onSubmit={create}><input value={name} onChange={e=>setName(e.target.value)} placeholder="Category name"/><input value={icon} onChange={e=>setIcon(e.target.value)} placeholder="Icon (optional)"/><button className="button button-primary" type="submit"><Plus size={13}/> Create</button></form><div className="admin-categories">{categories.map(c=><div className="admin-category-row" key={c.name}><span>{c.icon||"•"}</span><b>{c.name}</b></div>)}</div></div></section></div>;
}
