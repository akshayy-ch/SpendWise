import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { CalendarDays, ChevronLeft, ChevronRight, Plus, Search, TrendingUp } from "lucide-react";
import { incomeApi } from "../../api/incomeApi";
import { walletApi } from "../../api/walletApi";
import type { Income } from "../../types/income";
import type { Wallet } from "../../types/wallet";

const money = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 });

export default function IncomePage() {
  const [items, setItems] = useState<Income[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [source, setSource] = useState("");
  const [query, setQuery] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function load(nextPage = page) {
    setLoading(true); setError("");
    try {
      const response = await incomeApi.getIncome({ source: query || undefined, fromDate: fromDate ? new Date(fromDate).toISOString() : undefined, toDate: toDate ? new Date(toDate).toISOString() : undefined }, nextPage);
      setItems(response.content); setPage(response.page); setTotalPages(response.totalPages); setTotalElements(response.totalElements);
    } catch (err: any) { setError(err.response?.data?.message ?? "We couldn't load your income."); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(0); }, [query, fromDate, toDate]);

  function submit(e: FormEvent) { e.preventDefault(); setPage(0); setQuery(source.trim()); }

  return <div className="finance-page">
    <section className="page-intro"><div><span className="header-kicker">Cash inflow</span><h2>Income.</h2><p>{totalElements} recorded income entr{totalElements === 1 ? "y" : "ies"}</p></div><button className="button button-primary" onClick={() => setShowCreate(v => !v)}><Plus size={17}/> Add income</button></section>
    {showCreate && <CreateIncomeForm onCreated={() => { setShowCreate(false); load(0); }} />}
    <section className="income-toolbar"><form className="search-box" onSubmit={submit}><Search size={16}/><input value={source} onChange={e=>setSource(e.target.value)} placeholder="Search income sources..."/><button>Search</button></form><div className="date-filters"><label>From<input type="date" value={fromDate} onChange={e=>setFromDate(e.target.value)}/></label><label>To<input type="date" value={toDate} onChange={e=>setToDate(e.target.value)}/></label></div></section>
    {error ? <div className="inline-error"><TrendingUp size={15}/>{error}<button onClick={() => load(page)}>Retry</button></div> : <section className="expense-panel"><div className="table-head"><span><TrendingUp size={14}/> Income history</span><small>Sorted by newest first</small></div>{loading ? <IncomeSkeleton/> : items.length === 0 ? <div className="table-empty"><CalendarDays size={22}/><b>No income found</b><span>Try changing your search or date range.</span></div> : <div className="expense-table-wrap"><table className="expense-table"><thead><tr><th>Source</th><th>Wallet</th><th>Date</th><th className="amount-col">Amount</th></tr></thead><tbody>{items.map((item,index)=><tr key={item.incomeAt+item.source+item.amount+index}><td><b>{item.source}</b></td><td>{item.walletName}</td><td>{formatDate(item.incomeAt)}</td><td className="amount-col positive">+{money.format(item.amount)}</td></tr>)}</tbody></table></div>}{!loading&&totalPages>0&&<div className="pagination"><span>Page {page+1} of {totalPages}</span><div><button disabled={page===0} onClick={()=>load(page-1)}><ChevronLeft size={15}/></button><button disabled={page>=totalPages-1} onClick={()=>load(page+1)}><ChevronRight size={15}/></button></div></div>}</section>}
  </div>;
}

function CreateIncomeForm({ onCreated }: { onCreated: () => void }) {
  const [wallets,setWallets]=useState<Wallet[]>([]); const [source,setSource]=useState(""); const [amount,setAmount]=useState(""); const [description,setDescription]=useState(""); const [incomeAt,setIncomeAt]=useState(new Date().toISOString().slice(0,16)); const [walletName,setWalletName]=useState(""); const [error,setError]=useState(""); const [saving,setSaving]=useState(false);
  useEffect(()=>{walletApi.getWallets().then(data=>{const active=data.filter(w=>w.status==="ACTIVE");setWallets(active);if(active[0])setWalletName(active[0].walletName);}).catch((err:any)=>setError(err.response?.data?.message??"Couldn't load wallets."));},[]);
  async function submit(e:FormEvent){e.preventDefault();setError("");setSaving(true);try{await incomeApi.createIncome({source:source.trim(),walletName,description:description.trim()||undefined,amount:Number(amount),incomeAt:new Date(incomeAt).toISOString()});onCreated();}catch(err:any){setError(err.response?.data?.message??"Income couldn't be created.");}finally{setSaving(false);}}
  return <form className="expense-create" onSubmit={submit}><div className="expense-create-head"><div><span className="header-kicker">New transaction</span><h3>Add income.</h3></div><span>Wallet balance is updated by the backend.</span></div><div className="expense-form-grid"><label>Source<input required minLength={3} maxLength={50} value={source} onChange={e=>setSource(e.target.value)} placeholder="e.g. Salary"/></label><label>Amount<input required type="number" min="0.01" step="0.01" value={amount} onChange={e=>setAmount(e.target.value)} placeholder="0.00"/></label><label>Wallet<select required value={walletName} onChange={e=>setWalletName(e.target.value)}>{wallets.map(w=><option key={w.walletName} value={w.walletName}>{w.walletName} · {money.format(w.currentBalance)}</option>)}</select></label><label>Date & time<input required type="datetime-local" value={incomeAt} onChange={e=>setIncomeAt(e.target.value)}/></label><label>Description<input maxLength={255} value={description} onChange={e=>setDescription(e.target.value)} placeholder="Optional"/></label></div>{error&&<div className="form-error">{error}</div>}{wallets.length===0&&<div className="form-error">Create an active wallet before adding income.</div>}<div className="expense-form-actions"><button type="button" className="button button-secondary" onClick={onCreated}>Cancel</button><button className="button button-primary" disabled={saving||!walletName}>{saving?"Saving...":"Create income"}</button></div></form>;
}
function formatDate(value:string){return new Intl.DateTimeFormat("en-IN",{day:"2-digit",month:"short",year:"numeric"}).format(new Date(value));}
function IncomeSkeleton(){return <div className="expense-skeleton">{[1,2,3,4,5].map(i=><div key={i}/>)}</div>;}
