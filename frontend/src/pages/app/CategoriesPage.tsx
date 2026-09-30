import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { FolderKanban, Plus, Tag } from "lucide-react";
import { categoryApi } from "../../api/categoryApi";
import type { Category } from "../../types/category";

export default function CategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [filter, setFilter] = useState("ALL");
  const [name, setName] = useState("");
  const [icon, setIcon] = useState("");
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState("");

  async function load() {
    setError("");
    try { setCategories(await categoryApi.getCategories(filter)); } catch (err: any) { setError(err.response?.data?.message ?? "Couldn't load categories."); }
  }
  useEffect(() => { load(); }, [filter]);

  async function create(e: FormEvent) {
    e.preventDefault(); setError(""); setCreating(true);
    try { await categoryApi.createCategory(name.trim(), icon.trim() || undefined); setName(""); setIcon(""); await load(); }
    catch (err: any) { setError(err.response?.data?.message ?? "Category couldn't be created."); }
    finally { setCreating(false); }
  }

  return <div className="finance-page"><section className="page-intro"><div><span className="header-kicker">Organization</span><h2>Categories.</h2><p>Keep your spending organized your way.</p></div></section>
    <form className="category-create" onSubmit={create}><div className="category-create-title"><Tag size={17}/><div><b>Create a custom category</b><small>System categories remain available alongside your own.</small></div></div><input required minLength={3} maxLength={50} value={name} onChange={e=>setName(e.target.value)} placeholder="Category name"/><input minLength={3} maxLength={50} value={icon} onChange={e=>setIcon(e.target.value)} placeholder="Icon (optional)"/><button className="button button-primary" disabled={creating}><Plus size={15}/>{creating ? "Creating..." : "Create"}</button></form>
    {error && <div className="inline-error"><FolderKanban size={15}/>{error}<button onClick={load}>Retry</button></div>}
    <div className="category-toolbar"><div><b>Available categories</b><span>{categories.length} categories</span></div><select value={filter} onChange={e=>setFilter(e.target.value)}><option value="ALL">All</option><option value="SYSTEM">System</option><option value="CUSTOM">Custom</option></select></div>
    <div className="category-grid">{categories.map(category => <article className="category-card" key={category.name}><span className="category-card-icon">{category.icon || "•"}</span><div><b>{category.name}</b><small>{category.system ? "System category" : "Custom category"}</small></div></article>)}{categories.length===0&&<div className="wallet-empty">No categories found for this filter.</div>}</div>
  </div>;
}
