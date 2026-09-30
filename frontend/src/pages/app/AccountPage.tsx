import { useEffect, useState } from "react";
import { Mail, Phone, ShieldCheck, UserRound } from "lucide-react";
import { userApi } from "../../api/userApi";
import type { UserProfile } from "../../types/user";

export default function AccountPage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    userApi.getMe().then(setProfile).catch((err: any) => setError(err.response?.data?.message ?? "Couldn't load your account."));
  }, []);

  if (error) return <section className="dashboard-state"><div><span className="eyebrow">Account</span><h2>Couldn't load your account.</h2><p>{error}</p></div></section>;
  if (!profile) return <div className="finance-page"><div className="account-skeleton"/></div>;

  return <div className="finance-page">
    <section className="page-intro">
      <div><span className="header-kicker">Personal account</span><h2>Account.</h2><p>Your SpendWise account details.</p></div>
    </section>
    <section className="account-grid">
      <article className="account-card account-hero-card">
        <span className="account-avatar"><UserRound size={28}/></span>
        <div><span className="header-kicker">Signed in as</span><h3>{profile.name}</h3><p>@{profile.username}</p></div>
        <span className="status-pill active">{profile.role}</span>
      </article>
      <article className="account-card">
        <div className="account-detail"><Mail size={17}/><div><small>Email</small><b>{profile.email}</b></div></div>
        <div className="account-detail"><Phone size={17}/><div><small>Phone</small><b>{profile.phoneNumber}</b></div></div>
        <div className="account-detail"><ShieldCheck size={17}/><div><small>Role</small><b>{profile.role}</b></div></div>
      </article>
    </section>
  </div>;
}