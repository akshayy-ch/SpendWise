import { useEffect, useState } from "react";
import { ArrowDownLeft, ArrowUpRight, ChevronLeft, ChevronRight, CircleDollarSign, HandCoins, Users } from "lucide-react";
import { sharedFinanceApi } from "../../api/sharedFinanceApi";
import { walletApi } from "../../api/walletApi";
import { categoryApi } from "../../api/categoryApi";
import type { ExpenseShare, Settlement } from "../../types/shared";
import type { Wallet } from "../../types/wallet";
import type { Category } from "../../types/category";

const money = new Intl.NumberFormat("en-IN",{style:"currency",currency:"INR",maximumFractionDigits:2});

export default function SharedPage(){
 const [shares,setShares]=useState<ExpenseShare[]>([]);
 const [settlements,setSettlements]=useState<Settlement[]>([]);
 const [tab,setTab]=useState<"shares"|"settlements">("shares");
 const [page,setPage]=useState(0);
 const [totalPages,setTotalPages]=useState(0);
 const [loading,setLoading]=useState(true);
 const [error,setError]=useState("");
 const [settlingShare,setSettlingShare]=useState<ExpenseShare|null>(null);

 async function loadShares(){
  setLoading(true);setError("");
  try{setShares(await sharedFinanceApi.getMyShares());}
  catch(err:any){setError(err.response?.data?.message??"Couldn't load your expense shares.");}
  finally{setLoading(false);}
 }
 async function loadSettlements(next=page){
  setLoading(true);setError("");
  try{
   const r=await sharedFinanceApi.getMySettlements(next);
   setSettlements(r.content);setPage(r.page);setTotalPages(r.totalPages);
  }catch(err:any){setError(err.response?.data?.message??"Couldn't load settlements.");}
  finally{setLoading(false);}
 }
 useEffect(()=>{tab==="shares"?loadShares():loadSettlements(0);},[tab]);

 return <div className="finance-page">
  <section className="page-intro">
   <div><span className="header-kicker">Split & settle</span><h2>Shared finance.</h2><p>Track your outstanding shares and settlement history.</p></div>
  </section>
  <div className="shared-tabs">
   <button className={tab==="shares"?"active":""} onClick={()=>setTab("shares")}><HandCoins size={15}/> My shares</button>
   <button className={tab==="settlements"?"active":""} onClick={()=>setTab("settlements")}><CircleDollarSign size={15}/> Settlements</button>
  </div>
  {error&&<div className="inline-error"><Users size={15}/>{error}<button onClick={()=>tab==="shares"?loadShares():loadSettlements(page)}>Retry</button></div>}
  {loading?<div className="expense-skeleton">{[1,2,3,4,5].map(i=><div key={i}/>)}</div>:
   tab==="shares"?<SharesList shares={shares} settlingShare={settlingShare} onSettle={setSettlingShare} onSettled={()=>{setSettlingShare(null);loadShares();}}/>:
   <SettlementList settlements={settlements} page={page} totalPages={totalPages} onPage={loadSettlements}/>}
 </div>;
}

function SharesList({shares,settlingShare,onSettle,onSettled}:{shares:ExpenseShare[];settlingShare:ExpenseShare|null;onSettle:(share:ExpenseShare|null)=>void;onSettled:()=>void}){
 if(!shares.length)return <div className="table-empty"><HandCoins size={22}/><b>No expense shares</b><span>Shared expenses assigned to you will appear here.</span></div>;
 return <section className="share-grid">
  {shares.map(share=><article className="share-card" key={share.id}>
   <div className="share-card-top"><span className="transaction-icon"><Users size={15}/></span><span className={"status-pill "+share.status.toLowerCase()}>{share.status}</span></div>
   <h3>{share.expenseTitle}</h3>
   <div className="share-amounts">
    <div><small>Original share</small><b>{money.format(share.originalAmount)}</b></div>
    <div><small>Remaining</small><b className={share.remainingAmount>0?"negative":"positive"}>{money.format(share.remainingAmount)}</b></div>
   </div>
   <div className="progress"><span style={{width:Math.min(100,Math.max(0,(1-share.remainingAmount/share.originalAmount)*100))+"%"}}/></div>
   <div className="share-meta-row"><small className="share-meta">{share.percentage}% share · {share.groupName}</small>{share.remainingAmount>0&&share.status!=="SETTLED"&&<button className="table-action" onClick={()=>onSettle(share)}><CircleDollarSign size={13}/> Settle</button>}</div>
   {settlingShare?.id===share.id&&<SettlementForm share={share} onCancel={()=>onSettle(null)} onCreated={onSettled}/>}
  </article>)}
 </section>;
}

function SettlementForm({share,onCancel,onCreated}:{share:ExpenseShare;onCancel:()=>void;onCreated:()=>void}){
 const [wallets,setWallets]=useState<Wallet[]>([]);
 const [categories,setCategories]=useState<Category[]>([]);
 const [walletName,setWalletName]=useState("");
 const [categoryName,setCategoryName]=useState("");
 const [amount,setAmount]=useState(String(share.remainingAmount));
 const [error,setError]=useState("");
 const [saving,setSaving]=useState(false);

 useEffect(()=>{
  Promise.all([walletApi.getWallets(),categoryApi.getCategories("ALL")])
   .then(([walletData,categoryData])=>{
    const active=walletData.filter(wallet=>wallet.status==="ACTIVE");
    setWallets(active);setCategories(categoryData);
    if(active[0])setWalletName(active[0].walletName);
    if(categoryData[0])setCategoryName(categoryData[0].name);
   })
   .catch((err:any)=>setError(err.response?.data?.message??"Couldn't load wallets and categories."));
 },[]);

 async function submit(){
  setError("");
  const value=Number(amount);
  if(!walletName||!categoryName)return setError("Select an active wallet and category.");
  if(!Number.isFinite(value)||value<=0)return setError("Settlement amount must be greater than zero.");
  if(value>share.remainingAmount)return setError("Settlement amount cannot exceed the remaining share.");
  setSaving(true);
  try{
   await sharedFinanceApi.createSettlement(share.id,{amount:value,receiverId:share.receiverId,categoryName,walletName});
   onCreated();
  }catch(err:any){setError(err.response?.data?.message??"Couldn't create settlement.");}
  finally{setSaving(false);}
 }

 return <div className="settlement-editor">
  <div className="settlement-editor-head"><div><b>Settle this share</b><small>Payment goes to the original expense payer.</small></div><button className="icon-button" onClick={onCancel}><ChevronLeft size={15}/></button></div>
  <div className="share-form-grid">
   <label>Amount<input type="number" min="0.01" max={share.remainingAmount} step="0.01" value={amount} onChange={e=>setAmount(e.target.value)}/><small>Remaining: {money.format(share.remainingAmount)}</small></label>
   <label>Wallet<select value={walletName} onChange={e=>setWalletName(e.target.value)}>{wallets.map(wallet=><option key={wallet.walletName} value={wallet.walletName}>{wallet.walletName} · {money.format(wallet.currentBalance)}</option>)}</select></label>
   <label>Category<select value={categoryName} onChange={e=>setCategoryName(e.target.value)}>{categories.map(category=><option key={category.name} value={category.name}>{category.name}</option>)}</select></label>
  </div>
  {error&&<div className="form-error">{error}</div>}
  {wallets.length===0&&<div className="form-error">Create an active wallet before settling this share.</div>}
  <div className="expense-form-actions"><button className="button button-secondary" onClick={onCancel}>Cancel</button><button className="button button-primary" disabled={saving||wallets.length===0||categories.length===0} onClick={submit}>{saving?"Settling...":"Create settlement"}</button></div>
 </div>;
}

function SettlementList({settlements,page,totalPages,onPage}:{settlements:Settlement[];page:number;totalPages:number;onPage:(p:number)=>void}){
 return <section className="expense-panel">
  <div className="table-head"><span><CircleDollarSign size={14}/> Settlement history</span><small>Newest first</small></div>
  {!settlements.length?<div className="table-empty"><CircleDollarSign size={22}/><b>No settlements</b><span>Your settlement history will appear here.</span></div>:
   <div className="expense-table-wrap"><table className="expense-table"><thead><tr><th>Payer</th><th>Receiver</th><th>Category</th><th>Status</th><th>Date</th><th className="amount-col">Amount</th></tr></thead><tbody>{settlements.map(s=><tr key={s.id}><td><b>{s.payerName}</b></td><td>{s.receiverName}</td><td>{s.categoryName}</td><td><span className={"status-pill "+s.status.toLowerCase()}>{s.status}</span></td><td>{new Intl.DateTimeFormat("en-IN",{day:"2-digit",month:"short",year:"numeric"}).format(new Date(s.settledAt))}</td><td className="amount-col positive">{money.format(s.amount)}</td></tr>)}</tbody></table></div>}
  {totalPages>0&&<div className="pagination"><span>Page {page+1} of {totalPages}</span><div><button disabled={page===0} onClick={()=>onPage(page-1)}><ChevronLeft size={15}/></button><button disabled={page>=totalPages-1} onClick={()=>onPage(page+1)}><ChevronRight size={15}/></button></div></div>}
 </section>;
}
