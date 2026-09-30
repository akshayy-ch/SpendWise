import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { BarChart3, Bell, ChevronDown, CircleDollarSign, LayoutDashboard, LogOut, PieChart, Receipt, Tags, UserRound, Users, WalletCards } from "lucide-react";
import { useState } from "react";
import { useAuth } from "../context/AuthContext";

const nav = [
  { to: "/app", label: "Dashboard", icon: LayoutDashboard, end: true },
  { to: "/app/expenses", label: "Expenses", icon: Receipt },
  { to: "/app/income", label: "Income", icon: CircleDollarSign },
  { to: "/app/categories", label: "Categories", icon: Tags },
  { to: "/app/wallets", label: "Wallets", icon: WalletCards },
  { to: "/app/groups", label: "Groups", icon: Users },
  { to: "/app/shared", label: "Shared", icon: Users },
  { to: "/app/budgets", label: "Budgets", icon: PieChart },
  { to: "/app/analytics", label: "Analytics", icon: BarChart3 },
];

export default function AppShell() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [accountOpen, setAccountOpen] = useState(false);

  function signOut() {
    logout();
    navigate("/login");
  }

  return <div className="app-shell">
    <aside className="sidebar">
      <NavLink to="/app" className="brand sidebar-brand"><span className="brand-mark"><span/><span/><span/></span>SpendWise</NavLink>
      <div className="nav-section"><span>Workspace</span>{nav.map(({to,label,icon:Icon,end})=><NavLink key={to} to={to} end={end} className={({isActive})=>`side-link ${isActive?"active":""}`}><Icon size={18}/><span>{label}</span></NavLink>)}</div>
      <div className="sidebar-bottom">
        <NavLink to="/app/notifications" className={({isActive})=>`side-link ${isActive?"active":""}`}><Bell size={18}/><span>Notifications</span><span className="notification-dot"/></NavLink>
        <button className="side-link side-button" onClick={signOut}><LogOut size={18}/><span>Sign out</span></button>
      </div>
    </aside>
    <main className="app-main">
      <header className="app-header"><div><span className="header-kicker">SpendWise</span><h1>Financial workspace</h1></div><div className="header-actions">
        <button className="icon-button" aria-label="Notifications" onClick={()=>navigate("/app/notifications")}><Bell size={19}/></button>
        <div className="account-menu-wrap">
          <button className="profile-button profile-trigger" onClick={()=>setAccountOpen(v=>!v)} aria-expanded={accountOpen}><span className="avatar">A</span><span className="profile-copy"><b>Account</b><small>Personal</small></span><ChevronDown size={15}/></button>
          {accountOpen && <div className="account-menu"><button onClick={()=>{setAccountOpen(false);navigate("/app/account");}}><UserRound size={15}/> Account details</button><button onClick={signOut}><LogOut size={15}/> Sign out</button></div>}
        </div>
      </div></header>
      <div className="app-content"><Outlet /></div>
    </main>
  </div>;
}