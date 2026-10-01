import { useEffect, useMemo, useState } from "react";
import { Check, Users, X } from "lucide-react";
import { groupApi } from "../api/groupApi";
import { sharedFinanceApi } from "../api/sharedFinanceApi";
import { userApi } from "../api/userApi";
import type { Expense } from "../types/expense";
import type { Group, GroupMember } from "../types/group";

export default function ExpenseShareForm({ expense, onCreated, onCancel }: { expense: Expense; onCreated: () => void; onCancel: () => void }) {
  const [groups, setGroups] = useState<Group[]>([]);
  const [members, setMembers] = useState<GroupMember[]>([]);
  const [groupId, setGroupId] = useState("");
  const [selected, setSelected] = useState<string[]>([]);
  const [splitType, setSplitType] = useState<"EQUAL" | "PERCENTAGE" | "RANDOM">("EQUAL");
  const [percentages, setPercentages] = useState<Record<string, string>>({});
  const [profileUsername, setProfileUsername] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([groupApi.getMyGroups(), userApi.getMe()])
      .then(([groupsData, profile]) => { setGroups(groupsData); setProfileUsername(profile.username); if (groupsData[0]) setGroupId(groupsData[0].id); })
      .catch((err: any) => setError(err.response?.data?.message ?? "Couldn't load groups."));
  }, []);

  useEffect(() => {
    if (!groupId) { setMembers([]); return; }
    groupApi.getMembers(groupId)
      .then(data => { setMembers(data); setSelected([]); })
      .catch((err: any) => setError(err.response?.data?.message ?? "Couldn't load group members."));
  }, [groupId]);

  const eligible = useMemo(() => members.filter(member => member.username !== profileUsername), [members, profileUsername]);

  function toggle(id: string) {
    setSelected(current => current.includes(id) ? current.filter(value => value !== id) : [...current, id]);
  }

  function updatePercentage(id: string, value: string) {
    setPercentages(current => ({ ...current, [id]: value }));
  }

  async function submit() {
    setError("");
    if (!groupId) return setError("Select a group.");
    if (!selected.length) return setError("Select at least one group member.");
    if (splitType === "PERCENTAGE") {
      const values = selected.map(id => Number(percentages[id] || 0));
      if (values.some(value => value <= 0) || Math.abs(values.reduce((a, b) => a + b, 0) - 100) > 0.001) {
        return setError("Percentages must be greater than zero and add up to 100.");
      }
    }
    setSaving(true);
    try {
      await sharedFinanceApi.createExpenseShares(expense.id, groupId, {
        userIds: selected,
        splitType,
        percentages: splitType === "PERCENTAGE" ? selected.map(id => Number(percentages[id])) : undefined
      });
      onCreated();
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Couldn't create expense shares.");
    } finally { setSaving(false); }
  }

  return <section className="share-editor">
    <div className="share-editor-head"><div><span className="header-kicker">Split expense</span><h3>{expense.title}</h3><p>Share {new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(expense.amount)} with group members.</p></div><button className="icon-button" onClick={onCancel}><X size={16}/></button></div>
    <div className="share-form-grid">
      <label>Group<select value={groupId} onChange={e => setGroupId(e.target.value)}>{groups.map(group => <option key={group.id} value={group.id}>{group.name}</option>)}</select></label>
      <label>Split type<select value={splitType} onChange={e => setSplitType(e.target.value as "EQUAL" | "PERCENTAGE" | "RANDOM")}><option value="EQUAL">Equal</option><option value="PERCENTAGE">Percentage</option><option value="RANDOM">Random</option></select></label>
    </div>
    <div className="share-member-head"><b>Group members</b><span>{selected.length} selected</span></div>
    {eligible.length === 0 ? <div className="share-empty"><Users size={18}/><span>No other active members in this group. Add members from the Groups page first.</span></div> :
      <div className="share-members">{eligible.map(member => <div className={"share-member " + (selected.includes(member.userId) ? "selected" : "")} key={member.userId}>
        <button type="button" onClick={() => toggle(member.userId)}><span className="avatar">{member.userName.charAt(0).toUpperCase()}</span><span><b>{member.userName}</b><small>@{member.username}</small></span><span className="share-check">{selected.includes(member.userId) && <Check size={14}/>}</span></button>
        {splitType === "PERCENTAGE" && selected.includes(member.userId) && <input type="number" min="0.01" max="100" step="0.01" value={percentages[member.userId] ?? ""} onChange={e => updatePercentage(member.userId, e.target.value)} placeholder="%" />}
      </div>)}</div>}
    {error && <div className="form-error">{error}</div>}
    <div className="expense-form-actions"><button className="button button-secondary" onClick={onCancel}>Cancel</button><button className="button button-primary" disabled={saving || !eligible.length} onClick={submit}>{saving ? "Sharing..." : "Create expense shares"}</button></div>
  </section>;
}