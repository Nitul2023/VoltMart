import { FormEvent, useEffect, useRef, useState } from "react";
import { Link, NavLink, Outlet, useNavigate, useSearchParams } from "react-router-dom";
import { get } from "../api/client";
import { useAuth } from "../store/auth";
import { useCart } from "../store/cart";
import { ThemeToggle } from "./ThemeToggle";
import type { AutocompleteItem, CategoryView } from "../types";

export function Layout() {
  const { user, logout, isStaff } = useAuth();
  const { cart } = useCart();
  const [q, setQ] = useState("");
  const [hints, setHints] = useState<AutocompleteItem[]>([]);
  const [cats, setCats] = useState<CategoryView[]>([]);
  const nav = useNavigate();
  const [params] = useSearchParams();
  const activeCat = params.get("category") || "";
  const box = useRef<HTMLFormElement>(null);

  useEffect(() => {
    get<CategoryView[]>("/api/categories").then(setCats).catch(() => setCats([]));
  }, []);

  useEffect(() => {
    if (q.trim().length < 2) {
      setHints([]);
      return;
    }
    const t = setTimeout(() => {
      get<AutocompleteItem[]>(`/api/search/autocomplete?q=${encodeURIComponent(q)}`)
        .then(setHints)
        .catch(() => setHints([]));
    }, 180);
    return () => clearTimeout(t);
  }, [q]);

  function onSearch(e: FormEvent) {
    e.preventDefault();
    setHints([]);
    nav(`/catalog?q=${encodeURIComponent(q)}`);
  }

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main">Skip to content</a>
      <header className="site-header">
        <div className="container">
          <div className="topbar">
            <Link className="brand" to="/">VOLT<span>MART</span></Link>
            <form className="search" onSubmit={onSearch} ref={box} role="search">
              <input
                aria-label="Search products"
                placeholder="Search name, SKU, or category"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                autoComplete="off"
              />
              {hints.length > 0 && (
                <div className="suggest" role="listbox" aria-label="Search suggestions">
                  {hints.map((h) => (
                    <Link key={h.id} to={`/p/${h.slug}`} onClick={() => setHints([])}>
                      {h.imageUrl && <img src={h.imageUrl} alt="" width={40} height={40} style={{ width: 40, height: 40, objectFit: "cover", borderRadius: 6 }} />}
                      <span>{h.name}<br /><small className="muted">{h.sku} · {h.category}</small></span>
                    </Link>
                  ))}
                </div>
              )}
            </form>
            <nav className="nav" aria-label="Account">
              <NavLink to="/catalog">Shop</NavLink>
              <NavLink to="/compare">Compare</NavLink>
              <NavLink to="/account/wishlist">Wishlist</NavLink>
              <NavLink to="/cart">Cart{cart && cart.itemCount > 0 && <span className="badge">{cart.itemCount}</span>}</NavLink>
              <ThemeToggle />
              {user ? (
                <>
                  <NavLink to="/account">Hi, {user.firstName}</NavLink>
                  {isStaff && <NavLink to="/admin">CMS</NavLink>}
                  <button className="btn secondary" type="button" onClick={logout}>Sign out</button>
                </>
              ) : (
                <NavLink className="btn" to="/login">Sign in</NavLink>
              )}
            </nav>
          </div>
          <nav className="nav cat-nav" aria-label="Categories" style={{ paddingBottom: 12 }}>
            {cats.map((c) => (
              <Link key={c.id} className={activeCat === c.slug ? "active" : undefined} to={`/catalog?category=${c.slug}`}>{c.name}</Link>
            ))}
          </nav>
        </div>
      </header>
      <main id="main" className="container" style={{ flex: 1 }}>
        <Outlet />
      </main>
      <footer className="container footer">
        <div>© {new Date().getFullYear()} VoltMart Electronics</div>
        <div style={{ display: "flex", gap: 16 }}>
          <Link to="/pages/about">About</Link>
          <Link to="/pages/shipping">Shipping</Link>
          <Link to="/pages/returns">Returns</Link>
          <Link to="/pages/warranty">Warranty</Link>
        </div>
      </footer>
    </div>
  );
}
