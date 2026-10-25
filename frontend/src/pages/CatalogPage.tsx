import { useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { get } from "../api/client";
import { ProductCardView } from "../components/ProductCardView";
import { useQuery } from "../hooks/useQuery";
import type { BrandView, CategoryView, FilterView, PageResult, ProductCard } from "../types";

export function CatalogPage() {
  const [params, setParams] = useSearchParams();
  const q = params.get("q") || "";
  const category = params.get("category") || "";
  const brand = params.get("brand") || "";
  const sort = params.get("sort") || "featured";
  const page = Number(params.get("page") || 0);
  const featured = params.get("featured") === "true";
  const minPrice = params.get("minPrice") || "";
  const maxPrice = params.get("maxPrice") || "";
  const filters = params.get("filters") || "";

  const query = useMemo(() => {
    const p = new URLSearchParams();
    if (q) p.set("q", q);
    if (category) p.set("category", category);
    if (brand) p.set("brand", brand);
    if (featured) p.set("featured", "true");
    if (minPrice) p.set("minPrice", minPrice);
    if (maxPrice) p.set("maxPrice", maxPrice);
    if (filters) p.set("filters", filters);
    p.set("sort", sort);
    p.set("page", String(page));
    p.set("size", "12");
    return `/api/search?${p.toString()}`;
  }, [q, category, brand, sort, page, featured, minPrice, maxPrice, filters]);

  const result = useQuery(() => get<PageResult<ProductCard>>(query), [query]);
  const brands = useQuery(() => get<BrandView[]>("/api/brands"), []);
  const cats = useQuery(() => get<CategoryView[]>("/api/categories"), []);
  const filterDefs = useQuery(
    () => category ? get<FilterView[]>(`/api/categories/${category}/filters`) : Promise.resolve([]),
    [category]
  );

  function set(key: string, value: string) {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value); else next.delete(key);
    if (key !== "page") next.set("page", "0");
    if (key === "category") next.delete("filters");
    setParams(next);
  }

  function filterMap(): Record<string, string> {
    const map: Record<string, string> = {};
    if (!filters) return map;
    for (const part of filters.split(",")) {
      const [k, v] = part.split(":");
      if (k && v) map[k] = v;
    }
    return map;
  }

  function setFilter(slug: string, value: string) {
    const map = filterMap();
    if (value) map[slug] = value; else delete map[slug];
    const raw = Object.entries(map).map(([k, v]) => `${k}:${v}`).join(",");
    set("filters", raw);
  }

  const selected = cats.data?.find((c) => c.slug === category)
    || cats.data?.flatMap((c) => c.children || []).find((c) => c.slug === category);
  const parentOfSelected = cats.data?.find((c) => c.children?.some((child) => child.slug === category));
  const childLinks = (selected?.children && selected.children.length > 0)
    ? selected.children
    : (parentOfSelected?.children || []);

  return (
    <div className="layout">
      <aside className="filters">
        <h3>Refine</h3>
        <label>Category
          <select value={category} onChange={(e) => set("category", e.target.value)}>
            <option value="">All</option>
            {cats.data?.map((c) => (
              <optgroup key={c.id} label={c.name}>
                <option value={c.slug}>{c.name}</option>
                {c.children?.map((child) => (
                  <option key={child.id} value={child.slug}>{child.name}</option>
                ))}
              </optgroup>
            ))}
          </select>
        </label>
        {childLinks.length > 0 && (
          <div className="nav" style={{ flexWrap: "wrap", gap: 8, margin: "8px 0 12px" }}>
            {childLinks.map((child) => (
              <Link key={child.id} className="chip" to={`/catalog?category=${child.slug}`}>{child.name}</Link>
            ))}
          </div>
        )}
        <label>Brand
          <select value={brand} onChange={(e) => set("brand", e.target.value)}>
            <option value="">All</option>
            {brands.data?.map((b) => (
              <option key={b.id} value={b.slug}>{b.name}</option>
            ))}
          </select>
        </label>
        <label>Min price
          <input type="number" min={0} value={minPrice} onChange={(e) => set("minPrice", e.target.value)} placeholder="0" />
        </label>
        <label>Max price
          <input type="number" min={0} value={maxPrice} onChange={(e) => set("maxPrice", e.target.value)} placeholder="5000" />
        </label>
        {filterDefs.data && filterDefs.data.length > 0 && (
          <div>
            <h4>Category filters</h4>
            {filterDefs.data.map((f) => (
              <label key={f.id}>{f.name}{f.unit ? ` (${f.unit})` : ""}
                {f.filterType === "BOOLEAN" ? (
                  <select value={filterMap()[f.slug] || ""} onChange={(e) => setFilter(f.slug, e.target.value)}>
                    <option value="">Any</option>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                ) : (
                  <input value={filterMap()[f.slug] || ""} placeholder={f.filterType === "RANGE" ? "e.g. 6.7" : "Value"}
                    onChange={(e) => setFilter(f.slug, e.target.value)} />
                )}
              </label>
            ))}
          </div>
        )}
      </aside>
      <section>
        <div className="toolbar">
          <div>
            <h2 style={{ margin: 0 }}>{q ? `Results for “${q}”` : selected?.name || "Catalog"}</h2>
            <span className="muted">{result.data?.total ?? 0} products</span>
          </div>
          <label>Sort
            <select value={sort} onChange={(e) => set("sort", e.target.value)}>
              <option value="featured">Featured</option>
              <option value="price_asc">Price: low</option>
              <option value="price_desc">Price: high</option>
              <option value="name">Name</option>
              <option value="newest">Newest</option>
            </select>
          </label>
        </div>
        {result.error && <p className="error" role="alert">{result.error}</p>}
        {result.loading && <div className="grid products">{[1, 2, 3, 4].map((n) => <div key={n} className="skeleton" />)}</div>}
        {!result.loading && result.data?.items.length === 0 && (
          <div className="empty">
            <p>No products match these filters.</p>
            <Link className="btn" to="/catalog">Clear filters</Link>
          </div>
        )}
        <div className="grid products">
          {result.data?.items.map((p) => <ProductCardView key={p.id} p={p} />)}
        </div>
        <div className="pager">
          <button className="btn secondary" disabled={page <= 0} onClick={() => set("page", String(page - 1))}>Prev</button>
          <span>Page {page + 1} / {Math.max(result.data?.totalPages || 1, 1)}</span>
          <button className="btn secondary" disabled={page + 1 >= (result.data?.totalPages || 1)} onClick={() => set("page", String(page + 1))}>Next</button>
        </div>
      </section>
    </div>
  );
}
