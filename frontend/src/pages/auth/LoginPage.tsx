import { FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff, LockKeyhole, UserRound } from "lucide-react";
import { useAuth } from "../../context/AuthContext";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login({ username, password });
      navigate((location.state as { from?: string } | null)?.from ?? "/app", { replace: true });
    } catch (err: any) {
      const status = err.response?.status;
      setError(status === 403
        ? "Please verify your email before logging in."
        : status === 401
          ? "The username or password is incorrect."
          : err.response?.data?.message ?? "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return <AuthLayout eyebrow="Welcome back" title="Your finances, right where you left them." subtitle="Sign in to continue managing your money with SpendWise.">
    <form onSubmit={submit} className="auth-form">
      <label>Username<div className="input-wrap"><UserRound size={17}/><input value={username} onChange={e=>setUsername(e.target.value)} placeholder="Enter your username" autoComplete="username" required /></div></label>
      <label>Password<div className="input-wrap"><LockKeyhole size={17}/><input type={showPassword ? "text" : "password"} value={password} onChange={e=>setPassword(e.target.value)} placeholder="Enter your password" autoComplete="current-password" required /><button type="button" className="input-action" onClick={()=>setShowPassword(v=>!v)}>{showPassword ? <EyeOff size={17}/> : <Eye size={17}/>}</button></div></label>
      {error && <div className="form-error">{error}</div>}
      <button className="button button-primary auth-submit" disabled={loading}>{loading ? "Signing in..." : <>Sign in <ArrowRight size={17}/></>}</button>
      <p className="auth-switch">Don't have an account? <Link to="/register">Create one</Link></p>
    </form>
  </AuthLayout>;
}

export function AuthLayout({ eyebrow, title, subtitle, children }: { eyebrow: string; title: string; subtitle: string; children: React.ReactNode }) {
  return <main className="auth-page">
    <div className="auth-decoration"><span/><span/><span/></div>
    <Link to="/" className="auth-brand brand"><span className="brand-mark"><span/><span/><span/></span>SpendWise</Link>
    <div className="auth-shell">
      <div className="auth-side"><div className="eyebrow">SpendWise</div><h2>Money should feel <span>clear.</span></h2><p>Track your spending, stay on top of budgets and settle shared expenses without the spreadsheet chaos.</p><div className="auth-side-line"><span/> Built for your everyday money.</div></div>
      <section className="auth-card"><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p className="auth-subtitle">{subtitle}</p>{children}</section>
    </div>
  </main>;
}
