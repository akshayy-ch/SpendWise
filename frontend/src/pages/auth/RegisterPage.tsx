import { useState } from "react";
import type { FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail, Phone, UserRound, AtSign } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { AuthLayout } from "./LoginPage";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username:"", name:"", email:"", phoneNumber:"", password:"" });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function update(key: keyof typeof form, value: string) { setForm(prev => ({ ...prev, [key]: value })); }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await register(form);
      navigate("/verify-email", { state: { email: response.email } });
    } catch (err: any) {
      setError(err.response?.data?.message ?? "We couldn't create your account. Please check your details.");
    } finally { setLoading(false); }
  }

  return <AuthLayout eyebrow="Create your account" title="Start your clearer money life." subtitle="One account for personal spending, budgets and shared expenses.">
    <form onSubmit={submit} className="auth-form register-form">
      <div className="form-row">
        <label>Full name<div className="input-wrap"><UserRound size={17}/><input value={form.name} onChange={e=>update("name",e.target.value)} placeholder="Your name" minLength={3} required /></div></label>
        <label>Username<div className="input-wrap"><AtSign size={17}/><input value={form.username} onChange={e=>update("username",e.target.value)} placeholder="Choose a username" minLength={3} required /></div></label>
      </div>
      <label>Email<div className="input-wrap"><Mail size={17}/><input type="email" value={form.email} onChange={e=>update("email",e.target.value)} placeholder="you@example.com" required /></div></label>
      <label>Phone number<div className="input-wrap"><Phone size={17}/><input inputMode="numeric" value={form.phoneNumber} onChange={e=>update("phoneNumber",e.target.value.replace(/\D/g,""))} placeholder="10–15 digit number" minLength={10} maxLength={15} required /></div></label>
      <label>Password<div className="input-wrap"><LockKeyhole size={17}/><input type={showPassword ? "text" : "password"} value={form.password} onChange={e=>update("password",e.target.value)} placeholder="6–20 characters" minLength={6} maxLength={20} required /><button type="button" className="input-action" onClick={()=>setShowPassword(v=>!v)}>{showPassword ? <EyeOff size={17}/> : <Eye size={17}/>}</button></div></label>
      {error && <div className="form-error">{error}</div>}
      <button className="button button-primary auth-submit" disabled={loading}>{loading ? "Creating account..." : <>Create account <ArrowRight size={17}/></>}</button>
      <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
    </form>
  </AuthLayout>;
}
