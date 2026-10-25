import { NavLink, Outlet, Navigate } from "react-router-dom";
import { useAuth } from "../store/auth";
import { ThemeToggle } from "../components/ThemeToggle";

const links = [
  ["Dashboard", "/admin"],
  ["Products", "/admin/products"],
  ["Categories", "/admin/categories"],
  ["Inventory", "/admin/inventory"],
  ["Orders", "/admin/orders"],
  ["Returns", "/admin/returns"],
  ["Banners", "/admin/banners"],
  ["Pages", "/admin/pages"],
  ["Promotions", "/admin/promotions"],
  ["Customers", "/admin/customers"],
  ["Reports", "/admin/reports"]
];

export function AdminLayout() {
  const { isStaff, loading } = useAuth();
  if (loading) return <p>Loading…</p>;
  if (!isStaff) return <Navigate to="/login" replace />;
  return (
    <div className="admin">
      <nav>
        <strong>VoltMart CMS</strong>
        <div style={{ marginTop: 12 }}><ThemeToggle /></div>
        <div className="nav" style={{ flexDirection: "column", alignItems: "stretch", gap: 0, marginTop: 16 }}>
        {links.map(([label, to]) => (
          <NavLink key={to} to={to} end={to === "/admin"}>{label}</NavLink>
        ))}
        <NavLink to="/">Storefront</NavLink>
        </div>
      </nav>
      <main><Outlet /></main>
    </div>
  );
}
