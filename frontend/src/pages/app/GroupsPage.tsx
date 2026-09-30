import { FormEvent, useEffect, useState } from "react";
import { Archive, ChevronDown, Plus, Search, UserPlus, Users } from "lucide-react";
import { analyticsApi } from "../../api/analyticsApi";
import { groupApi } from "../../api/groupApi";
import type { Group, GroupMember, UserSearchResult } from "../../types/group";

export default function GroupsPage() {
  const [groups, setGroups] = useState<Group[]>([]);
  const [openGroups, setOpenGroups] = useState<Record<string, number>>({});
  const [expanded, setExpanded] = useState<string | null>(null);
  const [members, setMembers] = useState<GroupMember[]>([]);
  const [search, setSearch] = useState("");
  const [results, setResults] = useState<UserSearchResult[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [adding, setAdding] = useState(false);

  async function load() {
    setLoading(true); setError("");
    try {
      const [groupsData, openData] = await Promise.all([groupApi.getMyGroups(), analyticsApi.getOpenGroups()]);
      setGroups(groupsData);
      setOpenGroups(Object.fromEntries(openData.map(group => [group.groupId, group.openExpenseCount])));
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Couldn't load your groups.");
    } finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  async function toggleMembers(groupId: string) {
    if (expanded === groupId) { setExpanded(null); return; }
    try { setMembers(await groupApi.getMembers(groupId)); setExpanded(groupId); }
    catch (err: any) { setError(err.response?.data?.message ?? "Couldn't load group members."); }
  }

  async function findUsers() {
    if (search.trim().length < 2) return setResults([]);
    try { setResults(await groupApi.searchUsers(search.trim())); }
    catch (err: any) { setError(err.response?.data?.message ?? "Couldn't search users."); }
  }

  async function addMember(userId: string) {
    if (!expanded) return;
    setAdding(true); setError("");
    try {
      await groupApi.addMembers(expanded, [userId]);
      setMembers(await groupApi.getMembers(expanded));
      setSearch(""); setResults([]);
    } catch (err: any) { setError(err.response?.data?.message ?? "Couldn't add that user."); }
    finally { setAdding(false); }
  }

  async function archive(id: string) {
    try { await groupApi.archiveGroup(id); await load(); }
    catch (err: any) { setError(err.response?.data?.message ?? "Group couldn't be archived."); }
  }

  return <div className="finance-page">
    <section className="page-intro"><div><span className="header-kicker">Shared finance</span><h2>Groups.</h2><p>Your groups, members and open obligations.</p></div><button className="button button-primary" onClick={() => setShowForm(v => !v)}><Plus size={17}/> Create group</button></section>
    {showForm && <CreateGroupForm onCreated={() => { setShowForm(false); load(); }} />}
    {error && <div className="inline-error"><Users size={15}/>{error}<button onClick={load}>Retry</button></div>}
    <div className="shared-section-title"><div><b>My groups</b><span>Active groups you belong to</span></div></div>
    {loading ? <div className="wallet-grid">{[1,2,3].map(i => <div className="wallet-card wallet-skeleton" key={i}/>)}</div> :
      <div className="wallet-grid">{groups.map(group => <article className="wallet-card" key={group.id}>
        <div className="wallet-card-top"><span className="wallet-icon"><Users size={18}/></span><span className="status-pill active">{group.status}</span></div>
        <small>{group.description || "Shared expenses"}</small><h3>{group.name}</h3><strong>{openGroups[group.id] ?? 0} open expense{(openGroups[group.id] ?? 0) === 1 ? "" : "s"}</strong>
        <div className="wallet-card-bottom"><button onClick={() => toggleMembers(group.id)}><ChevronDown size={13}/> {expanded === group.id ? "Hide members" : "Members"}</button><button onClick={() => archive(group.id)}><Archive size={13}/> Archive</button></div>
        {expanded === group.id && <div className="group-members-panel">
          {members.map(member => <div className="group-member-row" key={member.userId}><span className="avatar">{member.userName.charAt(0).toUpperCase()}</span><div><b>{member.userName}</b><small>@{member.username}</small></div><span className="status-pill active">ACTIVE</span></div>)}
          <div className="member-add">
            <form onSubmit={e => { e.preventDefault(); findUsers(); }}><Search size={14}/><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search username..."/><button><UserPlus size={14}/></button></form>
            {results.map(user => <button className="user-result" key={user.id} disabled={adding} onClick={() => addMember(user.id)}><span>{user.name}</span><small>@{user.username}</small></button>)}
          </div>
        </div>}
      </article>)}{groups.length === 0 && <div className="wallet-empty">No groups yet. Create one to start sharing expenses.</div>}</div>}
  </div>;
}

function CreateGroupForm({ onCreated }: { onCreated: () => void }) {
  const [name, setName] = useState(""); const [description, setDescription] = useState(""); const [error, setError] = useState(""); const [saving, setSaving] = useState(false);
  async function submit(e: FormEvent) { e.preventDefault(); setError(""); setSaving(true); try { await groupApi.createGroup(name.trim(), description.trim() || undefined); onCreated(); } catch (err: any) { setError(err.response?.data?.message ?? "Group couldn't be created."); } finally { setSaving(false); } }
  return <form className="inline-form group-form" onSubmit={submit}><label>Group name<input required minLength={3} maxLength={50} value={name} onChange={e => setName(e.target.value)} placeholder="e.g. Goa Trip"/></label><label>Description<input minLength={3} maxLength={255} value={description} onChange={e => setDescription(e.target.value)} placeholder="Optional"/></label><button className="button button-primary" disabled={saving}>{saving ? "Creating..." : "Create group"}</button>{error && <span className="form-error">{error}</span>}</form>;
}