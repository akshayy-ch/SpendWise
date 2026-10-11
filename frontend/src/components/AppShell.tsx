import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { BarChart3, Bell, ChevronDown, CircleDollarSign, HandCoins, LayoutDashboard, LogOut, PieChart, Receipt, Shield, Tags, UserRound, Users, WalletCards } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { notificationApi } from "../api/notificationApi";
import { userApi } from "../api/userApi";
import type { UserProfile } from "../types/user";

interface NavItem { to: string; label: string; icon: LucideIcon; end?: boolean }

const sections: { label: string; items: NavItem[] }[] = [
  { label: "Money", items: [
    { to: "/app", label: "Dashboard", icon: LayoutDashboard, end: true },
    { to: "/app/expenses", label: "Expenses", icon: Receipt },
    { to: "/app/income", label: "Income", icon: CircleDollarSign },
    { to: "/app/wallets", label: "Wallets", icon: WalletCards },
    { to: "/app/categories", label: "Categories", icon: Tags },
  ] },
  { label: "Together", items: [
    { to: "/app/groups", label: "Groups", icon: Users },
    { to: "/app/shared", label: "Shared", icon: HandCoins },
  ] },
  { label: "Planning", items: [
    { to: "/app/budgets", label: "Budgets", icon: PieChart },
    { to: "/app/analytics", label: "Analytics", icon: BarChart3 },
  ] },
];

const otherTitles: Record<string, string> = {
  "/app/notifications": "Notifications",
  "/app/account": "Account",
  "/app/admin": "Admin",
};

const linkClass = ({ isActive }: { isActive: boolean }) => `side-link ${isActive ? "active" : ""}`;

export default function AppShell() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [accountOpen, setAccountOpen] = useState(false);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [unread, setUnread] = useState(0);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => { userApi.getMe().then(setProfile).catch(() => {}); }, []);
  useEffect(() => { notificationApi.unreadCount().then(setUnread).catch(() => {}); }, [location.pathname]);
  useEffect(() => { setAccountOpen(false); }, [location.pathname]);

  useEffect(() => {
    if (!accountOpen) return;
    const onPointer = (event: MouseEvent) => { if (!menuRef.current?.contains(event.target as Node)) setAccountOpen(false); };
    const onKey = (event: KeyboardEvent) => { if (event.key === "Escape") setAccountOpen(false); };
    document.addEventListener("mousedown", onPointer);
    document.addEventListener("keydown", onKey);
    return () => { document.removeEventListener("mousedown", onPointer); document.removeEventListener("keydown", onKey); };
  }, [accountOpen]);

  function signOut() {
    logout();
    navigate("/login");
  }

  const allItems = sections.flatMap(section => section.items);
  const current = allItems.find(item => item.end ? location.pathname === item.to : location.pathname.startsWith(item.to));
  const title = current?.label ?? otherTitles[location.pathname] ?? "SpendWise";
  const badge = unread > 9 ? "9+" : String(unread);
  const initial = (profile?.name || profile?.username || "").charAt(0).toUpperCase() || "?";
  const visibleSections = profile?.role === "ADMIN"
    ? [...sections, { label: "Administration", items: [{ to: "/app/admin", label: "Admin", icon: Shield }] }]
    : sections;

  return <div className="app-shell">
    <aside className="sidebar">
      <NavLink to="/app" className="brand sidebar-brand"><span className="brand-mark"><span/><span/><span/></span>SpendWise</NavLink>
      <nav aria-label="Main">
        {visibleSections.map(section => <div className="nav-section" key={section.label}>
          <span>{section.label}</span>
          {section.items.map(({ to, label, icon: Icon, end }) => <NavLink key={to} to={to} end={end} className={linkClass}><Icon size={18}/><span>{label}</span></NavLink>)}
        </div>)}
      </nav>
      <div className="sidebar-bottom">
        <NavLink to="/app/notifications" className={linkClass}><Bell size={18}/><span>Notifications</span>{unread > 0 && <span className="notification-dot" aria-label={`${unread} unread`}>{badge}</span>}</NavLink>
        <button className="side-link side-button" onClick={signOut}><LogOut size={18}/><span>Sign out</span></button>
      </div>
    </aside>
    <main className="app-main">
      <header className="app-header">
        <h1>{title}</h1>
        <div className="header-actions">
          <button className="icon-button" aria-label={unread > 0 ? `Notifications, ${unread} unread` : "Notifications"} onClick={() => navigate("/app/notifications")}>
            <Bell size={19}/>{unread > 0 && <span className="notification-dot" aria-hidden="true">{badge}</span>}
          </button>
          <div className="account-menu-wrap" ref={menuRef}>
            <button className="profile-button" onClick={() => setAccountOpen(v => !v)} aria-expanded={accountOpen} aria-haspopup="menu">
              <span className="avatar">{initial}</span>
              <span className="profile-copy">{profile?.name ?? "Account"}</span>
              <ChevronDown size={15}/>
            </button>
            {accountOpen && <div className="account-menu" role="menu">
              <button role="menuitem" onClick={() => navigate("/app/account")}><UserRound size={15}/> Account details</button>
              <button role="menuitem" onClick={signOut}><LogOut size={15}/> Sign out</button>
            </div>}
          </div>
        </div>
      </header>
      <div className="app-content"><Outlet /></div>
    </main>
  </div>;
}
