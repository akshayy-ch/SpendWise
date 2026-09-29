import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import AppShell from "./components/AppShell";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import VerifyEmailPage from "./pages/auth/VerifyEmailPage";
import DashboardPage from "./pages/app/DashboardPage";
import ExpensesPage from "./pages/app/ExpensesPage";
import WalletsPage from "./pages/app/WalletsPage";
import CategoriesPage from "./pages/app/CategoriesPage";
import IncomePage from "./pages/app/IncomePage";
import GroupsPage from "./pages/app/GroupsPage";

function Placeholder({ title }: { title: string }) {
  return <section className="placeholder-page"><span className="eyebrow">SpendWise</span><h2>{title}</h2><p>This workspace is being connected to the SpendWise API.</p></section>;
}

function LandingPage() {
  return <main className="landing">
    <header className="landing-nav"><a href="/" className="brand"><span className="brand-mark"><span/><span/><span/></span>SpendWise</a><nav><a href="#features">Features</a><a href="#preview">Preview</a></nav><div className="nav-actions"><a className="text-link" href="/login">Sign in</a><a className="button button-primary button-small" href="/register">Get started</a></div></header>
    <section className="hero"><div className="hero-copy"><div className="eyebrow"><span className="status-dot"/> Personal finance, without the noise</div><h1>Make every rupee<br/><span>make sense.</span></h1><p>SpendWise brings spending, budgets, wallets and shared expenses into one calm financial workspace.</p><div className="hero-actions"><a className="button button-primary" href="/register">Start using SpendWise <span>→</span></a><a className="button button-secondary" href="#preview">See the experience</a></div><div className="hero-proof"><span><b>01</b> Track everything</span><span><b>02</b> Understand patterns</span><span><b>03</b> Stay in control</span></div></div><div className="hero-visual"><div className="visual-glow"/><div className="mini-dashboard"><div className="mini-top"><span>September overview</span><span className="mini-pill">Live</span></div><div className="mini-balance"><small>Total balance</small><strong>₹84,240.50</strong><span>↑ 8.4% this month</span></div><div className="mini-chart"><i/><i/><i/><i/><i/><i/><i/><i/></div><div className="mini-bottom"><div><small>Spent</small><b>₹12,450</b></div><div><small>Budget left</small><b>₹7,550</b></div><div><small>Owed to you</small><b>₹1,850</b></div></div></div></div></section>
    <section className="feature-strip" id="features"><div><span className="feature-number">01</span><h3>One financial home</h3><p>Wallets, expenses, income and budgets stay connected instead of scattered across apps.</p></div><div><span className="feature-number">02</span><h3>Shared without friction</h3><p>Split group expenses, track who owes whom and settle up with a clear history.</p></div><div><span className="feature-number">03</span><h3>Clarity over complexity</h3><p>Analytics turn everyday transactions into a financial picture you can actually use.</p></div></section>
    <section className="preview-section" id="preview"><div className="section-heading"><div><span className="eyebrow">The workspace</span><h2>Everything in one view.</h2></div><p>A focused financial dashboard designed around the things you actually need to know.</p></div><div className="preview-window"><div className="preview-sidebar"><span className="preview-logo">S</span><span className="preview-active"/><span/><span/><span/><span/></div><div className="preview-content"><div className="preview-header"><span>Dashboard</span><span className="preview-avatar">A</span></div><div className="preview-cards"><div><small>Spent this month</small><b>₹12,450</b><em>12% from last month</em></div><div><small>Budget remaining</small><b>₹7,550</b><em>50% of monthly budget</em></div><div><small>You owe</small><b>₹3,200</b><em>4 open obligations</em></div></div><div className="preview-chart"><div className="chart-label"><b>Spending overview</b><small>September 2026</small></div><div className="preview-bars">{[30,44,39,58,49,74,55,82,65,88,71,95].map((h,i)=><span key={i} style={{height:`${h}%`}}/>)}</div></div></div></div></section>
    <footer><a className="brand" href="/"><span className="brand-mark"><span/><span/><span/></span>SpendWise</a><span>Personal finance, made clearer.</span></footer>
  </main>;
}

export default function App() {
  return <BrowserRouter><AuthProvider><Routes>
    <Route path="/" element={<LandingPage />} />
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />
    <Route path="/verify-email" element={<VerifyEmailPage />} />
    <Route element={<ProtectedRoute />}>
      <Route path="/app" element={<AppShell />}>
        <Route index element={<DashboardPage />} />
        <Route path="expenses" element={<ExpensesPage />} />
        <Route path="wallets" element={<WalletsPage />} />
        <Route path="categories" element={<CategoriesPage />} />
        <Route path="income" element={<IncomePage />} />
        <Route path="groups" element={<GroupsPage />} />
        <Route path="budgets" element={<Placeholder title="Budgets" />} />
        <Route path="analytics" element={<Placeholder title="Analytics" />} />
        <Route path="notifications" element={<Placeholder title="Notifications" />} />
      </Route>
    </Route>
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes></AuthProvider></BrowserRouter>;
}
