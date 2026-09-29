import { FormEvent, useEffect, useState } from "react";
import { Archive, Plus, Users } from "lucide-react";
import { analyticsApi } from "../../api/analyticsApi";
import { sharedFinanceApi } from "../../api/sharedFinanceApi";
import type { OpenGroup } from "../../types/analytics";

export default function GroupsPage() {
  const [groups,setGroups]=useState<OpenGroup[]>([]); const [showForm,setShowForm]=useState(false); const [error,setError]=useState(""); const [loading,setLoading]=useState(true);
  async function load(){setLoading(true);setError("");try{setGroups(await analyticsApi.getOpenGroups());}catch(err:any){setError(err.response?.data?.message??"Couldn't load open groups.");}finally{setLoading(false);}}
  useEffect(()=>{load();},[]);
  async function archive(id:string){try{await sharedFinanceApi.archiveGroup(id);await load();}catch(err:any){setError(err.response?.data?.message??"Group couldn't be archived.");}}
  return <div className="finance-page"><section className="page-intro"><div><span className="header-kicker">Shared finance</span><h2>Groups.</h2><p>Shared expenses and open obligations in one place.</p></div><button className="button button-primary" onClick={()=>setShowForm(v=>!v)}><Plus size={17}/> Create group</button></section>
    {showForm&&<CreateGroupForm onCreated={()=>{setShowForm(false);load();}}/>}
    {error&&<div className="inline-error"><Users size={15}/>{error}<button onClick={load}>Retry</button></div>}
    <div className="shared-section-title"><div><b>Open groups</b><span>Groups with outstanding expense shares</span></div></div>
    {loading?<div className="wallet-grid">{[1,2].map(i=><div className="wallet-card wallet-skeleton" key={i}/>)}</div>:<div className="wallet-grid">{groups.map(group=><article className="wallet-card" key={group.groupId}><div className="wallet-card-top"><span className="wallet-icon"><Users size={18}/></span><span className="status-pill active">OPEN</span></div><small>Expense sharing</small><h3>{group.groupName}</h3><strong>{group.openExpenseCount} open expense{group.openExpenseCount===1?"":"s"}</strong><div className="wallet-card-bottom"><span>Outstanding shares</span><button onClick={()=>archive(group.groupId)}><Archive size={13}/> Archive</button></div></article>)}{groups.length===0&&<div className="wallet-empty">No open groups with outstanding balances.</div>}</div>}
  </div>;
}
function CreateGroupForm({onCreated}:{onCreated:()=>void}){const[name,setName]=useState("");const[description,setDescription]=useState("");const[error,setError]=useState("");const[saving,setSaving]=useState(false);async function submit(e:FormEvent){e.preventDefault();setError("");setSaving(true);try{await sharedFinanceApi.createGroup(name.trim(),description.trim()||undefined);onCreated();}catch(err:any){setError(err.response?.data?.message??"Group couldn't be created.");}finally{setSaving(false);}}return <form className="inline-form group-form" onSubmit={submit}><label>Group name<input required minLength={3} maxLength={50} value={name} onChange={e=>setName(e.target.value)} placeholder="e.g. Goa Trip"/></label><label>Description<input minLength={3} maxLength={255} value={description} onChange={e=>setDescription(e.target.value)} placeholder="Optional"/></label><button className="button button-primary" disabled={saving}>{saving?"Creating...":"Create group"}</button>{error&&<span className="form-error">{error}</span>}</form>;}
