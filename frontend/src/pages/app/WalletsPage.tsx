import { FormEvent, useEffect, useState } from "react";
import { Archive, CreditCard, Landmark, Plus, RefreshCw, Wallet as WalletIcon } from "lucide-react";
import { walletApi } from "../../api/walletApi";
import type { Wallet } from "../../types/wallet";

const money = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 });

export default function WalletsPage() {
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);

  async function load() {
    setLoading(true); setError("");
    try { setWallets(await walletApi.getWallets()); } catch (err: any) { setError(err.response?.data?.message ?? "Couldn't load wallets."); } finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);

  async function toggle(wallet: Wallet) {
    try {
      if (wallet.status === "ACTIVE") await walletApi.archiveWallet(wallet.walletName);
      else await walletApi.activateWallet(wallet.walletName);
      await load();
    } catch (err: any) { setError(err.response?.data?.message ?? "Wallet status couldn't be changed."); }
  }

  return <div className="finance-page"><section className="page-intro"><div><span className="header-kicker">Money sources</span><h2>Wallets.</h2><p>Manage the places your money lives.</p></div><button className="button button-primary" onClick={() => setShowForm(v => !v)}><Plus size={17}/> New wallet</button></section>
    {showForm && <WalletForm onCreated={() => { setShowForm(false); load(); }} />}
    {error && <div className="inline-error"><RefreshCw size={15}/>{error}<button onClick={load}>Retry</button></div>}
    {loading ? <div className="wallet-grid">{[1,2,3].map(i => <div className="wallet-card wallet-skeleton" key={i}/>)}</div> : <div className="wallet-grid">{wallets.map(wallet => <WalletCard key={wallet.walletName} wallet={wallet} onToggle={() => toggle(wallet)} />)}{wallets.length === 0 && <div className="wallet-empty">No wallets yet. Create your first wallet to start tracking money.</div>}</div>}
  </div>;
}

function WalletCard({ wallet, onToggle }: { wallet: Wallet; onToggle: () => void }) {
  const Icon = wallet.type.toLowerCase().includes("bank") ? Landmark : wallet.type.toLowerCase().includes("card") ? CreditCard : WalletIcon;
  return <article className="wallet-card"><div className="wallet-card-top"><span className="wallet-icon"><Icon size={18}/></span><span className={"status-pill " + wallet.status.toLowerCase()}>{wallet.status}</span></div><small>{wallet.type}</small><h3>{wallet.walletName}</h3><strong>{money.format(wallet.currentBalance)}</strong><div className="wallet-card-bottom"><span>Created {new Intl.DateTimeFormat("en-IN",{day:"2-digit",month:"short",year:"numeric"}).format(new Date(wallet.createdAt))}</span><button onClick={onToggle}><Archive size={13}/>{wallet.status === "ACTIVE" ? "Archive" : "Activate"}</button></div></article>;
}

function WalletForm({ onCreated }: { onCreated: () => void }) {
  const [name, setName] = useState(""); const [type, setType] = useState("CASH"); const [balance, setBalance] = useState(""); const [error, setError] = useState("");
  async function submit(e: FormEvent) { e.preventDefault(); setError(""); try { await walletApi.createWallet({ walletName: name.trim(), type, initialBalance: balance ? Number(balance) : undefined }); onCreated(); } catch (err: any) { setError(err.response?.data?.message ?? "Wallet couldn't be created."); } }
  return <form className="inline-form" onSubmit={submit}><div><label>Wallet name<input required minLength={3} maxLength={100} value={name} onChange={e=>setName(e.target.value)} placeholder="e.g. Main Bank"/></label></div><label>Type<select value={type} onChange={e=>setType(e.target.value)}><option>CASH</option><option>BANK</option><option>CREDIT_CARD</option><option>DEBIT_CARD</option></select></label><label>Initial balance<input type="number" min="0" step="0.01" value={balance} onChange={e=>setBalance(e.target.value)} placeholder="0.00"/></label><button className="button button-primary" type="submit">Create wallet</button>{error&&<span className="form-error">{error}</span>}</form>;
}
