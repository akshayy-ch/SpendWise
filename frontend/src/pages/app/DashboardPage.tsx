import { ArrowUpRight, CreditCard, IndianRupee, Plus, TrendingDown, TrendingUp, Users, Wallet } from "lucide-react";

const activities = [
  ["Dinner with friends", "Food", "−₹850"],
  ["Monthly salary", "Income", "+₹45,000"],
  ["Uber ride", "Transport", "−₹320"],
  ["Groceries", "Shopping", "−₹1,240"],
];

export default function DashboardPage() {
  return <div className="dashboard">
    <section className="page-intro"><div><span className="header-kicker">Overview</span><h2>Good evening, Akshay.</h2><p>Here's how your money is looking this month.</p></div><button className="button button-primary"><Plus size={17}/> Add expense</button></section>
    <section className="metric-grid">
      <Metric title="Spent this month" value="₹12,450" note="12% from last month" icon={<TrendingDown size={18}/>} />
      <Metric title="Budget remaining" value="₹7,550" note="50% of your monthly budget" icon={<IndianRupee size={18}/>} />
      <Metric title="You owe" value="₹3,200" note="4 open obligations" icon={<Users size={18}/>} negative />
      <Metric title="You are owed" value="₹1,850" note="2 people owe you" icon={<TrendingUp size={18}/>} />
    </section>
    <section className="dashboard-grid">
      <article className="panel spending-panel"><div className="panel-head"><div><h3>Spending overview</h3><p>September 2026</p></div><button className="ghost-button">This month <ArrowUpRight size={14}/></button></div><div className="chart-placeholder"><div className="chart-bars">{[35,48,42,64,55,76,60,88,68,92,78,100].map((h,i)=><span key={i} style={{height:`${h}%`}} />)}</div><div className="chart-axis"><span>Week 1</span><span>Week 2</span><span>Week 3</span><span>Week 4</span></div></div></article>
      <article className="panel budget-panel"><div className="panel-head"><div><h3>Monthly budget</h3><p>Overall spending</p></div><Wallet size={18}/></div><div className="budget-ring"><strong>50%</strong><span>used</span></div><div className="budget-line"><span>₹12,450 spent</span><b>₹20,000</b></div><div className="progress"><span style={{width:"62%"}} /></div><small>₹7,550 remaining</small></article>
    </section>
    <section className="dashboard-grid lower"><article className="panel"><div className="panel-head"><div><h3>Recent activity</h3><p>Your latest transactions</p></div><button className="ghost-button">View all</button></div><div className="activity-list">{activities.map(([name,category,amount])=><div className="dashboard-activity" key={name}><span className="transaction-icon"><CreditCard size={16}/></span><div><b>{name}</b><small>{category} · Today</small></div><strong className={amount.startsWith("+")?"positive":"negative"}>{amount}</strong></div>)}</div></article><article className="panel"><div className="panel-head"><div><h3>Group obligations</h3><p>Open shared expenses</p></div><Users size={18}/></div><div className="obligation"><span className="avatar avatar-purple">R</span><div><b>Rahul</b><small>Dinner split</small></div><strong className="negative">₹1,200</strong></div><div className="obligation"><span className="avatar avatar-blue">S</span><div><b>Sarah</b><small>Trip expenses</small></div><strong className="negative">₹2,000</strong></div><button className="full-ghost">View all obligations</button></article></section>
  </div>;
}

function Metric({title,value,note,icon,negative}:{title:string;value:string;note:string;icon:React.ReactNode;negative?:boolean}) {
 return <article className="metric-card"><div className="metric-icon">{icon}</div><span>{title}</span><strong className={negative?"negative":""}>{value}</strong><small>{note}</small></article>;
}
