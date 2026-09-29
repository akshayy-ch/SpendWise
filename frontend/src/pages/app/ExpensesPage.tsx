import { FormEvent, useEffect, useState } from "react";
import { ChevronLeft, ChevronRight, Filter, Plus, Search, SlidersHorizontal, X } from "lucide-react";
import { expenseApi } from "../../api/expenseApi";
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

  async function loadExpenses(nextPage = page) {
    setLoading(true); setError("");
    const filters: ExpenseFilters = { search: query || undefined, status: status || undefined, categoryName: categoryName || undefined, walletName: walletName || undefined };
    try {
      const response = await expenseApi.getExpenses(filters, nextPage);
      setItems(response.content); setPage(response.page); setTotalPages(response.totalPages); setTotalElements(response.totalElements);
    } catch (err: any) {
      setError(err.response?.data?.message ?? "We couldn't load your expenses.");
    } finally { setLoading(false); }
  }

  useEffect(() => { loadExpenses(0); }, [query, status, categoryName, walletName]);

  function submitSearch(event: FormEvent) {
    event.preventDefault();
    setPage(0); setQuery(search.trim());
  }

  return <div className="finance-page">
    <section className="page-intro"><div><span className="header-kicker">Transactions</span><h2>Expenses.</h2><p>{totalElements} recorded expense{totalElements === 1 ? "" : "s"}</p></div><button className="button button-primary"><Plus size={17}/> Add expense</button></section>
    <section className="expense-toolbar">
      <form className="search-box" onSubmit={submitSearch}><Search size={16}/><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search expenses..." /><button type="submit">Search</button></form>
      <div className="filter-group"><select value={status} onChange={e => { setPage(0); setStatus(e.target.value as ExpenseStatus | ""); }}><option value="">All statuses</option><option value="ACTIVE">Active</option><option value="VOIDED">Voided</option></select><input value={categoryName} onChange={e => { setPage(0); setCategoryName(e.target.value); }} placeholder="Category" /><input value={walletName} onChange={e => { setPage(0); setWalletName(e.target.value); }} placeholder="Wallet" /></div>
    </section>
    {error ? <div className="inline-error"><X size={16}/>{error}<button onClick={() => loadExpenses(page)}>Retry</button></div> : <section className="expense-panel">
      <div className="table-head"><span><SlidersHorizontal size={14}/> Latest expenses</span><small>Sorted by newest first</small></div>
      {loading ? <ExpenseSkeleton/> : items.length === 0 ? <div className="table-empty"><Filter size={22}/><b>No expenses found</b><span>Try changing your search or filters.</span></div> : <div className="expense-table-wrap"><table className="expense-table"><thead><tr><th>Expense</th><th>Category</th><th>Wallet</th><th>Status</th><th>Date</th><th className="amount-col">Amount</th></tr></thead><tbody>{items.map(expense => <tr key={expense.expenseAt + expense.title + expense.amount}><td><b>{expense.title}</b></td><td>{expense.categoryName}</td><td>{expense.walletName}</td><td><span className={"status-pill " + expense.status.toLowerCase()}>{expense.status}</span></td><td>{formatDate(expense.expenseAt)}</td><td className="amount-col negative">−{money.format(expense.amount)}</td></tr>)}</tbody></table></div>}
      {!loading && totalPages > 0 && <div className="pagination"><span>Page {page + 1} of {totalPages}</span><div><button disabled={page === 0} onClick={() => loadExpenses(page - 1)}><ChevronLeft size={15}/></button><button disabled={page >= totalPages - 1} onClick={() => loadExpenses(page + 1)}><ChevronRight size={15}/></button></div></div>}
    </section>}
  </div>;
}

function formatDate(value: string) { return new Intl.DateTimeFormat("en-IN", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(value)); }
function ExpenseSkeleton() { return <div className="expense-skeleton">{[1,2,3,4,5].map(i => <div key={i}/>)}</div>; }
