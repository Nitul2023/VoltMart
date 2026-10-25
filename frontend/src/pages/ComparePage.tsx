import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { get } from "../api/client";
import type { ProductDetail } from "../types";
import { money } from "../utils/money";

export function ComparePage() {
  const [items, setItems] = useState<ProductDetail[]>([]);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    const ids: string[] = JSON.parse(localStorage.getItem("vm_compare") || "[]");
    if (!ids.length) {
      setLoaded(true);
      return;
    }
    get<ProductDetail[]>(`/api/products/compare?ids=${ids.join(",")}`)
      .then(setItems)
      .catch(() => setItems([]))
      .finally(() => setLoaded(true));
  }, []);

  function clear() {
    localStorage.removeItem("vm_compare");
    setItems([]);
  }

  if (!loaded) return <p>Loading comparison…</p>;
  if (!items.length) {
    return (
      <div className="empty" style={{ margin: "40px 0" }}>
        <h1>Nothing to compare</h1>
        <p className="muted">Open a product and tap Compare (up to 4 devices).</p>
        <Link className="btn" to="/catalog">Browse catalog</Link>
      </div>
    );
  }

  const specKeys = Array.from(new Set(items.flatMap((p) => p.specs.map((s) => `${s.groupName}|${s.specKey}`))));

  return (
    <div style={{ overflowX: "auto" }}>
      <div className="toolbar">
        <h1 style={{ margin: 0 }}>Compare</h1>
        <button className="btn secondary" type="button" onClick={clear}>Clear list</button>
      </div>
      <table className="table">
        <thead>
          <tr>
            <th>Spec</th>
            {items.map((p) => <th key={p.id}><Link to={`/p/${p.slug}`}>{p.name}</Link><div className="muted">{money(p.basePrice)}</div></th>)}
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Brand</td>
            {items.map((p) => <td key={p.id}>{p.brand?.name}</td>)}
          </tr>
          <tr>
            <td>Warranty</td>
            {items.map((p) => <td key={p.id}>{p.warrantyMonths} mo</td>)}
          </tr>
          {specKeys.map((k) => {
            const [g, key] = k.split("|");
            return (
              <tr key={k}>
                <td>{g} · {key}</td>
                {items.map((p) => (
                  <td key={p.id}>{p.specs.find((s) => s.groupName === g && s.specKey === key)?.specValue || "—"}</td>
                ))}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
