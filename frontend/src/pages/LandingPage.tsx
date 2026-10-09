import { Link } from "react-router-dom";

const items = [
  ["Villa, 2 nights", "₹13,360"],
  ["Beach shack dinner", "₹3,240"],
  ["Scooter rental, 2 days", "₹1,800"],
];

const people = [
  { name: "Aarav", note: "Paid the bill", tag: "Paid", tone: "is-paid" },
  { name: "Meera", note: "₹4,600 of ₹4,600", tag: "Settled", tone: "is-settled" },
  { name: "Isha", note: "₹2,800 of ₹4,600", tag: "₹1,800 left", tone: "" },
  { name: "Kabir", note: "₹0 of ₹4,600", tag: "₹4,600 left", tone: "" },
];

const features = [
  ["Wallets and expenses", "Cash, bank accounts and cards each keep their own balance. Log an expense and the wallet updates for you."],
  ["Budgets with limits", "Set an overall budget and a limit for each category. SpendWise notifies you when you go over."],
  ["Split any expense", "Share a bill equally, by percentage or at random across a group, and see who still owes what."],
  ["Settle in parts", "Pay back a share in one go or in instalments. Every settlement is kept in your history."],
];

const steps = [
  ["Log the expense", "Pick a wallet and category, then enter the amount."],
  ["Split it with a group", "Choose who shares the cost and how it divides."],
  ["Settle up", "Members pay their share from any of their wallets."],
];

export default function LandingPage() {
  return <main className="landing">
    <header className="landing-nav">
      <Link to="/" className="brand"><span className="brand-mark"><span/><span/><span/></span>SpendWise</Link>
      <nav><a href="#features">Features</a><a href="#how">How splitting works</a></nav>
      <div className="nav-actions"><Link className="text-link" to="/login">Sign in</Link><Link className="button button-primary button-small" to="/register">Create account</Link></div>
    </header>

    <section className="hero">
      <div className="hero-copy">
        <h1>Know where every rupee went, and who still owes you.</h1>
        <p>SpendWise tracks your wallets, expenses and budgets, then splits shared bills with your group and keeps count until everyone has settled.</p>
        <div className="hero-actions"><Link className="button button-primary" to="/register">Create your account</Link><a className="button button-secondary" href="#how">See how splitting works</a></div>
      </div>
      <div className="hero-visual" aria-label="Example of a split expense for a Goa trip">
        <div className="receipt-wrap"><div className="receipt">
          <div className="receipt-head"><h3>Goa trip</h3><span>Split equally, 4 people</span></div>
          <ul>{items.map(([name, amount]) => <li key={name}><span>{name}</span><b>{amount}</b></li>)}</ul>
          <div className="receipt-total"><span>Total</span><span>₹18,400</span></div>
          <p className="receipt-split">₹4,600 each</p>
          <ul className="receipt-people">{people.map(p => <li key={p.name}><div><b>{p.name}</b><small>{p.note}</small></div><em className={p.tone}>{p.tag}</em></li>)}</ul>
        </div></div>
      </div>
    </section>

    <section className="section" id="features">
      <div className="section-heading"><h2>What it keeps track of</h2><p>Everything you spend, earn and share, in one account instead of a notes app and a group chat.</p></div>
      <dl className="feature-list">{features.map(([title, text]) => <div key={title}><dt>{title}</dt><dd>{text}</dd></div>)}</dl>
    </section>

    <section className="section" id="how">
      <div className="section-heading"><h2>How splitting works</h2><p>A shared expense takes three steps, and the balance updates after each one.</p></div>
      <ol className="steps">{steps.map(([title, text]) => <li key={title}><b>{title}</b><p>{text}</p></li>)}</ol>
    </section>

    <footer className="landing-footer"><Link className="brand" to="/"><span className="brand-mark"><span/><span/><span/></span>SpendWise</Link><span>Track spending, set budgets and split bills.</span></footer>
  </main>;
}
