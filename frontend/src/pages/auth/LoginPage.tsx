import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Eye, EyeOff, LockKeyhole, UserRound } from "lucide-react";
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
  const registered = (location.state as { registered?: boolean } | null)?.registered ?? false;

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login({ username, password });
      navigate((location.state as { from?: string } | null)?.from ?? "/app", { replace: true });
    } catch (err: any) {
      const status = err.response?.status;
      setError(status === 401
        ? "The username or password is incorrect."
        : err.response?.data?.message ?? "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return <AuthLayout eyebrow="Welcome back" title="Sign in to SpendWise" subtitle="Welcome back. Enter your details to continue.">
    <form onSubmit={submit} className="auth-form">
      <label>Username<div className="input-wrap"><UserRound size={17}/><input value={username} onChange={e=>setUsername(e.target.value)} placeholder="Enter your username" autoComplete="username" required /></div></label>
      <label>Password<div className="input-wrap"><LockKeyhole size={17}/><input type={showPassword ? "text" : "password"} value={password} onChange={e=>setPassword(e.target.value)} placeholder="Enter your password" autoComplete="current-password" required /><button type="button" className="input-action" aria-label={showPassword ? "Hide password" : "Show password"} onClick={()=>setShowPassword(v=>!v)}>{showPassword ? <EyeOff size={17}/> : <Eye size={17}/>}</button></div></label>
      {registered && !error && <div className="form-success" role="status">Account created. Sign in to continue.</div>}
      {error && <div className="form-error">{error}</div>}
      <button className="button button-primary auth-submit" disabled={loading}>{loading ? "Signing in..." : "Sign in"}</button>
      <p className="auth-switch">Don't have an account? <Link to="/register">Create one</Link></p>
    </form>
  </AuthLayout>;
}

export function AuthLayout({ eyebrow, title, subtitle, children }: { eyebrow: string; title: string; subtitle: string; children: React.ReactNode }) {
  return <main className="auth-page">
    <div className="auth-decoration"><span/><span/><span/></div>
    <Link to="/" className="auth-brand brand"><span className="brand-mark"><span/><span/><span/></span>SpendWise</Link>
    <div className="auth-shell">
      <div className="auth-side"><h2>Log what you spend. Split what you share. See what is left.</h2><p>Wallets, budgets and group expenses live in one account, so nothing gets lost in a chat thread or a spreadsheet.</p></div>
      <section className="auth-card"><h1>{title}</h1><p className="auth-subtitle">{subtitle}</p>{children}</section>
    </div>
  </main>;
}
