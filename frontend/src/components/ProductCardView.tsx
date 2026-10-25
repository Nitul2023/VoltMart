import { Link } from "react-router-dom";
import type { ProductCard } from "../types";
import { availabilityLabel, money } from "../utils/money";

export function ProductCardView({ p }: { p: ProductCard }) {
  return (
    <article className="card">
      <Link to={`/p/${p.slug}`}>
        <img src={p.imageUrl || "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800"} alt={p.name} loading="lazy" width={460} height={345} />
      </Link>
      <div className="body">
        <span className="chip">{p.brand}</span>
        <Link to={`/p/${p.slug}`}><strong>{p.name}</strong></Link>
        <span className="muted">{p.shortDescription}</span>
        <div className="price">
          {p.compareAtPrice && p.compareAtPrice > p.basePrice && <s>{money(p.compareAtPrice)}</s>}
          {money(p.basePrice)}
        </div>
        <small className="muted">{availabilityLabel(p.availability)} · {p.warrantyMonths} mo warranty</small>
      </div>
    </article>
  );
}
