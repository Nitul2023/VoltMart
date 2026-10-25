import { Link } from "react-router-dom";
import { get } from "../api/client";
import { ProductCardView } from "../components/ProductCardView";
import { useQuery } from "../hooks/useQuery";
import type { BannerView, ProductCard } from "../types";

export function HomePage() {
  const heroes = useQuery(() => get<BannerView[]>("/api/banners?placement=HOME_HERO"), []);
  const strip = useQuery(() => get<BannerView[]>("/api/banners?placement=HOME_STRIP"), []);
  const featured = useQuery(() => get<ProductCard[]>("/api/products/featured"), []);
  const hero = heroes.data?.[0];

  return (
    <>
      {hero ? (
        <section className="hero">
          <img src={hero.imageUrl} alt={hero.title} />
          <div className="hero-copy">
            <h1>{hero.title}</h1>
            <p>{hero.subtitle}</p>
            {hero.ctaUrl && <Link className="btn" to={hero.ctaUrl}>{hero.ctaLabel || "Shop"}</Link>}
          </div>
        </section>
      ) : heroes.loading ? (
        <div className="skeleton" style={{ height: 320, margin: "24px 0" }} />
      ) : null}
      {strip.data?.[0] && (
        <div className="strip">
          <div>
            <strong>{strip.data[0].title}</strong>
            <div className="muted">{strip.data[0].subtitle}</div>
          </div>
          {strip.data[0].ctaUrl && <Link className="btn secondary" to={strip.data[0].ctaUrl}>{strip.data[0].ctaLabel}</Link>}
        </div>
      )}
      <h2 style={{ marginTop: 32 }}>Featured circuit</h2>
      {featured.error && <p className="error" role="alert">{featured.error}</p>}
      {featured.loading && <div className="grid products">{[1, 2, 3, 4].map((n) => <div key={n} className="skeleton" />)}</div>}
      <div className="grid products">
        {featured.data?.map((p) => <ProductCardView key={p.id} p={p} />)}
      </div>
    </>
  );
}
