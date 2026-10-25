import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { get, post } from "../api/client";
import { useQuery } from "../hooks/useQuery";
import { ReviewForm } from "./AccountPages";
import { useAuth } from "../store/auth";
import { useCart } from "../store/cart";
import type { PageResult, ProductDetail, ReviewView } from "../types";
import { money } from "../utils/money";

export function ProductPage() {
  const { slug = "" } = useParams();
  const product = useQuery(() => get<ProductDetail>(`/api/products/${slug}`), [slug]);
  const p = product.data;
  const [img, setImg] = useState(0);
  const [variantId, setVariantId] = useState<string>("");
  const [msg, setMsg] = useState("");
  const { add } = useCart();
  const { user } = useAuth();
  const reviews = useQuery(
    () => p ? get<PageResult<ReviewView>>(`/api/products/${p.id}/reviews`) : Promise.resolve(null),
    [p?.id]
  );

  const variant = useMemo(
    () => p?.variants.find((v) => v.id === variantId) || p?.variants[0],
    [p, variantId]
  );

  useEffect(() => {
    if (p?.seoTitle) document.title = p.seoTitle;
    else if (p?.name) document.title = `${p.name} | VoltMart`;
    return () => { document.title = "VoltMart | Electronics"; };
  }, [p]);

  if (product.loading) return <p>Loading product…</p>;
  if (!p) return <p className="error" role="alert">{product.error || "Not found"}</p>;

  const activeImg = p.images[img] || p.images[0];

  async function addCart() {
    if (!variant || !p) return;
    try {
      await add(variant.id, 1);
      setMsg("Added to cart");
    } catch (e) {
      setMsg((e as Error).message);
    }
  }

  async function wish() {
    if (!p) return;
    try {
      await post(`/api/account/wishlist/${p.id}`);
      setMsg("Saved to wishlist");
    } catch (e) {
      setMsg((e as Error).message);
    }
  }

  function compare() {
    if (!p) return;
    const raw = localStorage.getItem("vm_compare") || "[]";
    const ids: string[] = JSON.parse(raw);
    if (!ids.includes(p.id)) ids.push(p.id);
    localStorage.setItem("vm_compare", JSON.stringify(ids.slice(-4)));
    setMsg("Added to comparison");
  }

  return (
    <article>
      <div className="pdp">
        <div className="gallery">
          {activeImg && <img src={activeImg.url} alt={activeImg.altText || p.name} />}
          <div className="thumbs">
            {p.images.map((im, i) => (
              <button type="button" key={im.id} className={i === img ? "active" : ""} onClick={() => setImg(i)} aria-label={`Show image ${i + 1}`}>
                <img src={im.url} alt="" />
              </button>
            ))}
          </div>
        </div>
        <div>
          <span className="chip">{p.brand?.name} · {p.category?.name}</span>
          <h1>{p.name}</h1>
          <p className="muted">{p.shortDescription}</p>
          <div className="stars" aria-label={`${p.averageRating || 0} out of 5 stars`}>
            {"★★★★★".slice(0, Math.round(p.averageRating || 0))}
            <span className="muted"> {p.reviewCount} reviews</span>
          </div>
          <p className="price" style={{ fontSize: "1.4rem" }}>
            {variant?.compareAtPrice && <s>{money(variant.compareAtPrice)}</s>}
            {money(variant?.price ?? p.basePrice)}
          </p>
          <p className="muted">SKU {variant?.sku || p.sku} · {p.warrantyMonths} month warranty</p>
          <p>{variant && variant.availableQty > 0 ? `${variant.availableQty} available` : "Out of stock"}</p>
          <label>Variant
            <select value={variant?.id} onChange={(e) => setVariantId(e.target.value)}>
              {p.variants.map((v) => (
                <option key={v.id} value={v.id}>{v.name} — {money(v.price)}</option>
              ))}
            </select>
          </label>
          <div style={{ display: "flex", gap: 10, marginTop: 16, flexWrap: "wrap" }}>
            <button className="btn" onClick={addCart} disabled={!variant || variant.availableQty < 1}>Add to cart</button>
            {user && <button className="btn ghost" onClick={wish}>Wishlist</button>}
            <button className="btn secondary" onClick={compare}>Compare</button>
          </div>
          {msg && <p className={msg.startsWith("Added") || msg.startsWith("Saved") ? "success" : "error"} role="status">{msg}</p>}
          <p>{p.description}</p>
        </div>
      </div>
      <h2>Technical specifications</h2>
      {p.specs.length === 0 ? <p className="muted">No specifications published yet.</p> : (
        <table className="specs">
          <tbody>
            {p.specs.map((s, i) => (
              <tr key={i}><td className="muted">{s.groupName} · {s.specKey}</td><td>{s.specValue}</td></tr>
            ))}
          </tbody>
        </table>
      )}
      <h2>Reviews</h2>
      {reviews.data?.items.length === 0 && <p className="muted">Be the first to review this product.</p>}
      {reviews.data?.items.map((r) => (
        <div key={r.id} className="card" style={{ padding: 16, marginBottom: 10 }}>
          <strong>{r.title || "Review"}</strong> <span className="muted">by {r.author}</span>
          {r.verified && <span className="chip" style={{ marginLeft: 8 }}>Verified</span>}
          <div className="stars">{"★".repeat(r.rating)}</div>
          <p>{r.body}</p>
        </div>
      ))}
      {user ? (
        <div style={{ marginTop: 16, maxWidth: 520 }}>
          <h3>Write a review</h3>
          <ReviewForm productId={p.id} />
        </div>
      ) : (
        <p className="muted"> <Link to="/login">Sign in</Link> to write a review.</p>
      )}
    </article>
  );
}
