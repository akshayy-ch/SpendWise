import { useEffect, useMemo, useState } from "react";
import { ArrowUpRight, CircleDollarSign, CreditCard, IndianRupee, Plus, TrendingDown, TrendingUp, Users, Wallet } from "lucide-react";
import { analyticsApi } from "../../api/analyticsApi";
import type { DashboardData, RecentActivity } from "../../types/analytics";

const emptyData: DashboardData = { spentThisMonth: 0, budgetRemaining: null, youOwe: 0, youAreOwed: 0, spendingByCategory: [], openGroups: [], recentActivity: [] };
const money = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 });

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData>(emptyData);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadDashboard() {
    setError(""); setLoading(true);
    try {
      const [spentThisMonth, budgetRemaining, youOwe, youAreOwed, spendingByCategory, openGroups, recentActivity] = await Promise.all([
        analyticsApi.getSpentThisMonth(), analyticsApi.getBudgetRemaining(), analyticsApi.getYouOwe(), analyticsApi.getYouAreOwed(),
        analyticsApi.getSpendingByCategory(), analyticsApi.getOpenGroups(), analyticsApi.getRecentActivity(),
      ]);
      setData({ spentThisMonth, budgetRemaining, youOwe, youAreOwed, spendingByCategory, openGroups, recentActivity });
    } catch (err: any) {
      setError(err.response?.data?.message ?? "We couldn't load your dashboard. Please try again.");
    } finally { setLoading(false); }
  }

  useEffect(() => { loadDashboard(); }, []);

  const month = new Intl.DateTimeFormat("en-IN", { month: "long", year: "numeric" }).format(new Date());
  const budgetTotal = useMemo(() => data.spendingByCategory.reduce((sum, item) => sum + item.budget, 0), [data.spendingByCategory]);
  const spentFromBudget = budgetTotal > 0 ? Math.max(0, budgetTotal - (data.budgetRemaining ?? budgetTotal)) : 0;
  const budgetPercent = budgetTotal > 0 ? Math.min(100, Math.max(0, spentFromBudget / budgetTotal * 100)) : 0;

  if (loading) return <DashboardSkeleton />;
  if (error) return <section className="dashboard-state"><div><span className="eyebrow">Dashboard</span><h2>Something needs a retry.</h2><p>{error}</p><button className="button button-primary" onClick={loadDashboard}>Try again</button></div></section>;

  return <div className="dashboard">
    <section className="page-intro"><div><span className="header-kicker">Overview</span><h2>Your financial snapshot.</h2><p>{month} · live from SpendWise</p></div><button className="button button-primary"><Plus size={17}/> Add expense</button></section>
    <section className="metric-grid">
      <Metric title="Spent this month" value={money.format(data.spentThisMonth)} note="Expenses and settlements" icon={<TrendingDown size={18}/>} />
      <Metric title="Budget remaining" value={data.budgetRemaining === null ? "—" : money.format(data.budgetRemaining)} note={data.budgetRemaining === null ? "No active overall budget" : Math.round(100 - budgetPercent) + "% remaining"} icon={<IndianRupee size={18}/>} />
      <Metric title="You owe" value={money.format(data.youOwe)} note="Outstanding shared expenses" icon={<Users size={18}/>} negative />
      <Metric title="You are owed" value={money.format(data.youAreOwed)} note="Outstanding this month" icon={<TrendingUp size={18}/>} />
    </section>
    <section className="dashboard-grid">
      <article className="panel spending-panel"><div className="panel-head"><div><h3>Spending by category</h3><p>Current budget period</p></div><ArrowUpRight size={15}/></div>{data.spendingByCategory.length === 0 ? <EmptyState text="No category budgets or spending yet."/> : <div className="category-list">{data.spendingByCategory.slice(0, 6).map(item => { const percent = item.budget > 0 ? Math.min(100, item.spent / item.budget * 100) : 0; return <div className="category-row" key={item.budgetId}><div><b>{item.categoryName}</b><span>{money.format(item.spent)} of {money.format(item.budget)}</span></div><div className="category-progress"><span style={{ width: percent + "%" }}/></div><strong>{Math.round(percent)}%</strong></div>; })}</div>}</article>
      <article className="panel budget-panel"><div className="panel-head"><div><h3>Budget status</h3><p>Overall budget</p></div><Wallet size={18}/></div><div className="budget-ring" style={{ background: "radial-gradient(circle at center,#101513 57%,transparent 58%),conic-gradient(var(--green) 0 " + budgetPercent + "%,#1e2923 " + budgetPercent + "% 100%)" }}><strong>{Math.round(budgetPercent)}%</strong><span>used</span></div><div className="budget-line"><span>{money.format(spentFromBudget)} spent</span><b>{budgetTotal ? money.format(budgetTotal) : "—"}</b></div><div className="progress"><span style={{ width: budgetPercent + "%" }}/></div><small>{data.budgetRemaining === null ? "Create an overall budget to track progress." : money.format(data.budgetRemaining) + " remaining"}</small></article>
    </section>
    <section className="dashboard-grid lower"><article className="panel"><div className="panel-head"><div><h3>Recent activity</h3><p>Last 7 days</p></div><button className="ghost-button">View all</button></div>{data.recentActivity.length === 0 ? <EmptyState text="No activity in the last 7 days."/> : <div className="activity-list">{data.recentActivity.slice(0, 6).map(activity => <Activity key={activity.id} activity={activity} />)}</div>}</article><article className="panel"><div className="panel-head"><div><h3>Group obligations</h3><p>Groups with open shares</p></div><Users size={18}/></div>{data.openGroups.length === 0 ? <EmptyState text="No open group obligations."/> : data.openGroups.slice(0, 4).map((group, index) => <div className="obligation" key={group.groupId}><span className={"avatar " + (index % 2 ? "avatar-blue" : "avatar-purple")}>{group.groupName.charAt(0).toUpperCase()}</span><div><b>{group.groupName}</b><small>{group.openExpenseCount} open expense{group.openExpenseCount === 1 ? "" : "s"}</small></div><ArrowUpRight size={14}/></div>)}</article></section>
  </div>;
}

function Activity({ activity }: { activity: RecentActivity }) {
  const date = new Intl.DateTimeFormat("en-IN", { day: "numeric", month: "short" }).format(new Date(activity.occurredAt));
  const icon = activity.type === "INCOME" ? <TrendingUp size={16}/> : activity.type === "SETTLEMENT" ? <CircleDollarSign size={16}/> : <CreditCard size={16}/>;
  return <div className="dashboard-activity"><span className="transaction-icon">{icon}</span><div><b>{activity.title}</b><small>{activity.type.toLowerCase()} · {date}</small></div><strong className={activity.amount >= 0 ? "positive" : "negative"}>{activity.amount >= 0 ? "+" : ""}{money.format(Math.abs(activity.amount))}</strong></div>;
}

function Metric({ title, value, note, icon, negative }: { title: string; value: string; note: string; icon: React.ReactNode; negative?: boolean }) {
  return <article className="metric-card"><div className="metric-icon">{icon}</div><span>{title}</span><strong className={negative ? "negative" : ""}>{value}</strong><small>{note}</small></article>;
}
function EmptyState({ text }: { text: string }) { return <div className="empty-state">{text}</div>; }
function DashboardSkeleton() { return <div className="dashboard"><div className="page-intro skeleton-intro"><div/><div/></div><div className="metric-grid">{[1,2,3,4].map(i => <div className="metric-card skeleton-card" key={i}/>)}</div><div className="dashboard-grid"><div className="panel skeleton-panel"/><div className="panel skeleton-panel"/></div></div>; }
