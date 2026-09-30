import { FormEvent, useEffect, useState } from "react";
import { ChevronLeft, ChevronRight, Filter, Plus, Search, SlidersHorizontal, Users, X } from "lucide-react";
import { expenseApi } from "../../api/expenseApi";
import { walletApi } from "../../api/walletApi";
import { categoryApi } from "../../api/categoryApi";
import ExpenseShareForm from "../../components/ExpenseShareForm";
import type { Wallet } from "../../types/wallet";
import type { Category } from "../../types/category";
import type { Expense, ExpenseFilters } from "../../types/expense";
import type { ExpenseStatus } from "../../types/expenseStatus";

const money = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 });

export default function ExpensesPage() {
  const [items, setItems] = useState<Expense[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<ExpenseStatus | "">("");
  const [categoryName, setCategoryName] = useState("");
  const [walletName, setWalletName] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [shareExpense, setShareExpense] = useState<Expense | null>(null);

  async function loadExpenses(nextPage = page) {
    setLoading(true); setError("");
    const filters: ExpenseFilters = { search: query || undefined, status: status || undefined, categoryName: categoryName || undefined, walletName: walletName || undefined };
    try {
      const response = await expenseApi.getExpenses(filters, nextPage);
      setItems(response.content); setPage(response.page); setTotalPages(response.totalPages); setTotalElements(response.totalElements);
    } catch (err: any) { setError(err.response?.data?.message ?? "We couldn't load your expenses."); }
    finally { setLoading(false); }
  }

  useEffect(() => { loadExpenses(0); }, [query, status, categoryName, walletName]);

  function submitSearch(event: FormEvent) { event.preventDefault(); setPage(0); setQuery(search.trim()); }

  return <div className="finance-page">
    <section className="page-intro"><div><span className="header-kicker">Transactions</span><h2>Expenses.</h2><p>{totalElements} recorded expense{totalElements === 1 ? "" : "s"}</p></div><button className="button button-primary" onClick={() => setShowCreate(v => !v)}><Plus size={17}/> Add expense</button></section>
    <section className="expense-toolbar">
      <form className="search-box" onSubmit={submitSearch}><Search size={16}/><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search expenses..." /><button type="submit">Search</button></form>
      <div className="filter-group"><select value={status} onChange={e => { setPage(0); setStatus(e.target.value as ExpenseStatus | ""); }}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="VOIDED">Voided</option></select><input value={categoryName} onChange={e => { setPage(0); setCategoryName(e.target.value); }} placeholder="Category" /><input value={walletName} onChange={e => { setPage(0); setWalletName(e.target.value); }} placeholder="Wallet" /></div>
    </section>
    {showCreate && <CreateExpenseForm onCreated={() => { setShowCreate(false); loadExpenses(0); }} />}
    {shareExpense && <ExpenseShareForm expense={shareExpense} onCreated={() => { setShareExpense(null); loadExpenses(page); }} onCancel={() => setShareExpense(null)} />}
    {error ? <div className="inline-error"><X size={16}/>{error}<button onClick={() => loadExpenses(page)}>Retry</button></div> : <section className="expense-panel">
      <div className="table-head"><span><SlidersHorizontal size={14}/> Latest expenses</span><small>Sorted by newest first</small></div>
      {loading ? <ExpenseSkeleton/> : items.length === 0 ? <div className="table-empty"><Filter size={22}/><b>No expenses found</b><span>Try changing your search or filters.</span></div> : <div className="expense-table-wrap"><table className="expense-table"><thead><tr><th>Expense</th><th>Category</th><th>Wallet</th><th>Status</th><th>Date</th><th className="amount-col">Amount</th><th></th></tr></thead><tbody>{items.map(expense => <tr key={expense.id}><td><b>{expense.title}</b></td><td>{expense.categoryName}</td><td>{expense.walletName}</td><td><span className={"status-pill " + expense.status.toLowerCase()}>{expense.status}</span></td><td>{formatDate(expense.expenseAt)}</td><td className="amount-col negative">−{money.format(expense.amount)}</td><td className="expense-actions">{expense.status === "ACTIVE" && <button className="table-action" onClick={() => setShareExpense(expense)}><Users size={13}/> Split</button>}</td></tr>)}</tbody></table></div>}
      {!loading && totalPages > 0 && <div className="pagination"><span>Page {page + 1} of {totalPages}</span><div><button disabled={page === 0} onClick={() => loadExpenses(page - 1)}><ChevronLeft size={15}/></button><button disabled={page >= totalPages - 1} onClick={() => loadExpenses(page + 1)}><ChevronRight size={15}/></button></div></div>}
    </section>}
  </div>;
}

function CreateExpenseForm({ onCreated }: { onCreated: () => void }) {
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [title, setTitle] = useState(""); const [amount, setAmount] = useState(""); const [walletName, setWalletName] = useState(""); const [categoryName, setCategoryName] = useState("");
  const [expenseAt, setExpenseAt] = useState(new Date().toISOString().slice(0,16)); const [description, setDescription] = useState(""); const [error, setError] = useState(""); const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([walletApi.getWallets(), categoryApi.getCategories("ALL")]).then(([wallets, categories]) => {
      const active = wallets.filter(wallet => wallet.status === "ACTIVE"); setWallets(active); setCategories(categories);
      if (active[0]) setWalletName(active[0].walletName); if (categories[0]) setCategoryName(categories[0].name);
    }).catch((err: any) => setError(err.response?.data?.message ?? "Couldn't load wallets and categories."));
  }, []);

  async function submit(e: FormEvent) {
    e.preventDefault(); setError(""); setSaving(true);
    try {
      await expenseApi.createExpense({ title: title.trim(), description: description.trim() || undefined, amount: Number(amount), expenseAt: new Date(expenseAt).toISOString(), walletName, categoryName, status: "ACTIVE" });
      onCreated();
    } catch (err: any) { setError(err.response?.data?.message ?? "Expense couldn't be created."); }
    finally { setSaving(false); }
  }

  return <form className="expense-create" onSubmit={submit}><div className="expense-create-head"><div><span className="header-kicker">New transaction</span><h3>Add an expense.</h3></div><span>Wallet balance is updated by the backend.</span></div>
    <div className="expense-form-grid"><label>Title<input required minLength={3} maxLength={100} value={title} onChange={e=>setTitle(e.target.value)} placeholder="e.g. Dinner"/></label><label>Amount<input required type="number" min="0.01" step="0.01" value={amount} onChange={e=>setAmount(e.target.value)} placeholder="0.00"/></label><label>Wallet<select required value={walletName} onChange={e=>setWalletName(e.target.value)}>{wallets.map(wallet=><option key={wallet.walletName} value={wallet.walletName}>{wallet.walletName} · {money.format(wallet.currentBalance)}</option>)}</select></label><label>Category<select required value={categoryName} onChange={e=>setCategoryName(e.target.value)}>{categories.map(category=><option key={category.name} value={category.name}>{category.name}</option>)}</select></label><label>Date & time<input required type="datetime-local" value={expenseAt} onChange={e=>setExpenseAt(e.target.value)}/></label><label>Description<input maxLength={255} value={description} onChange={e=>setDescription(e.target.value)} placeholder="Optional"/></label></div>
    {error && <div className="form-error">{error}</div>}{wallets.length === 0 && <div className="form-error">Create an active wallet before adding an expense.</div>}<div className="expense-form-actions"><button type="button" className="button button-secondary" onClick={onCreated}>Cancel</button><button className="button button-primary" disabled={saving || !walletName || !categoryName}>{saving ? "Saving..." : "Create expense"}</button></div>
  </form>;
}
function formatDate(value: string) { return new Intl.DateTimeFormat("en-IN", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(value)); }
function ExpenseSkeleton() { return <div className="expense-skeleton">{[1,2,3,4,5].map(i => <div key={i}/>)}</div>; }