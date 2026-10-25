import { FormEvent, useState } from "react";
import { del, get, patch, post } from "../api/client";
import { useQuery } from "../hooks/useQuery";
import type { AuditView, BrandView, CategoryView, CustomerView, OrderView, ProductDetail, ReportSummary, ReturnView } from "../types";
import { money } from "../utils/money";

export function AdminDashboard() {
  const report = useQuery(() => get<ReportSummary>("/api/admin/reports/summary"), []);
  const r = report.data;
  return (
    <section>
      <h1>Operations</h1>
      {r && (
        <div className="grid products">
          <Stat label="Orders today" value={String(r.ordersToday)} />
          <Stat label="Revenue today" value={money(r.revenueToday)} />
          <Stat label="Customers" value={String(r.customers)} />
          <Stat label="Low stock" value={String(r.lowStockVariants)} />
          <Stat label="Open returns" value={String(r.openReturns)} />
        </div>
      )}
    </section>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return <div className="card"><div className="body"><div className="muted">{label}</div><strong style={{ fontSize: "1.6rem" }}>{value}</strong></div></div>;
}

export function AdminProducts() {
  const list = useQuery(() => get<{ content: ProductDetail[] }>("/api/admin/products?size=100"), []);
  const categories = useQuery(() => get<CategoryView[]>("/api/admin/categories"), []);
  const brands = useQuery(() => get<BrandView[]>("/api/admin/brands"), []);
  const [msg, setMsg] = useState("");
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  async function deleteProduct(id: string, name: string) {
    if (!window.confirm(`Delete "${name}" from the active product list? This will hide it from the storefront but preserve historical order records.`)) return;
    setMsg("");
    setDeletingId(id);
    try {
      await del<void>(`/api/admin/products/${id}`);
      const refreshed = await get<{ content: ProductDetail[] }>("/api/admin/products?size=100");
      list.setData(refreshed);
      setMsg(`Product "${name}" deleted successfully.`);
    } catch (err) {
      setMsg(err instanceof Error ? `Could not delete product: ${err.message}` : "Could not delete product. Check the backend terminal.");
    } finally {
      setDeletingId(null);
    }
  }

  async function create(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setMsg("");
    setSaving(true);
    const form = e.currentTarget;
    const fd = new FormData(form);
    const sku = String(fd.get("sku") || "").trim();
    const name = String(fd.get("name") || "").trim();
    const price = Number(fd.get("basePrice"));
    const categoryId = Number(fd.get("categoryId"));
    const brandId = Number(fd.get("brandId"));
    const compareValue = String(fd.get("compareAtPrice") || "").trim();
    const imageUrl = String(fd.get("imageUrl") || "").trim();

    if (!Number.isFinite(price) || price <= 0) {
      setMsg("Enter a valid price greater than zero.");
      setSaving(false);
      return;
    }
    if (!categoryId || !brandId) {
      setMsg("Please select an existing category and brand. If the dropdowns are empty, add them first.");
      setSaving(false);
      return;
    }

    try {
      await post<ProductDetail>("/api/admin/products", {
        sku,
        name,
        shortDescription: fd.get("shortDescription") || "",
        description: fd.get("description") || "",
        categoryId,
        brandId,
        basePrice: price,
        compareAtPrice: compareValue ? Number(compareValue) : null,
        warrantyMonths: 12,
        active: true,
        featured: false,
        images: imageUrl ? [{ url: imageUrl, altText: name, sortOrder: 0 }] : [],
        variants: [{ sku: `${sku}-DEF`, name: "Default", price, stockQty: 10, active: true }]
      });
      const refreshed = await get<{ content: ProductDetail[] }>("/api/admin/products?size=100");
      list.setData(refreshed);
      setMsg("Product added successfully and the list has been refreshed.");
      form.reset();
    } catch (err) {
      setMsg(err instanceof Error ? `Could not add product: ${err.message}` : "Could not add product. Check the backend terminal for details.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section>
      <h1>Products</h1>
      {(categories.error || brands.error || list.error) && (
        <p className="error">Could not load admin data: {categories.error || brands.error || list.error}. Check that the backend is running and you are signed in with an admin/editor account.</p>
      )}
      <form className="form" onSubmit={create}>
        <input name="sku" required maxLength={60} placeholder="SKU (max 60 characters, must be unique)" />
        <input name="name" required placeholder="Product name" />
        <input name="shortDescription" placeholder="Short description" />
        <textarea name="description" placeholder="Description" />
        <select name="categoryId" required defaultValue="">
          <option value="" disabled>{categories.loading ? "Loading categories…" : "Select category"}</option>
          {categories.data?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select name="brandId" required defaultValue="">
          <option value="" disabled>{brands.loading ? "Loading brands…" : "Select brand"}</option>
          {brands.data?.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
        </select>
        <input name="basePrice" required type="number" min="0.01" step="0.01" placeholder="Price" />
        <input name="compareAtPrice" type="number" min="0" step="0.01" placeholder="Compare-at price (optional)" />
        <input name="imageUrl" type="url" placeholder="Image URL (optional)" />
        <button className="btn" type="submit" disabled={saving || categories.loading || brands.loading}>
          {saving ? "Saving product…" : "Create product"}
        </button>
        {msg && <p role="status" className={msg.startsWith("Could not") || msg.startsWith("Enter") || msg.startsWith("Please") ? "error" : "success"}>{msg}</p>}
      </form>
      <h2>Saved products</h2>
      {list.loading && <p>Loading products…</p>}
      <table className="table">
        <thead><tr><th>SKU</th><th>Name</th><th>Category</th><th>Brand</th><th>Price</th><th>Actions</th></tr></thead>
        <tbody>
          {list.data?.content?.map((p) => (
            <tr key={p.id}>
              <td>{p.sku}</td><td>{p.name}</td><td>{p.category?.name || "—"}</td><td>{p.brand?.name || "—"}</td><td>{money(p.basePrice)}</td>
              <td><button className="btn" type="button" disabled={deletingId === p.id} onClick={() => deleteProduct(p.id, p.name)}>
                {deletingId === p.id ? "Deleting…" : "Delete"}
              </button></td>
            </tr>
          ))}
          {!list.loading && !list.error && !list.data?.content?.length && <tr><td colSpan={6}>No products found.</td></tr>}
        </tbody>
      </table>
    </section>
  );
}

export function AdminCategories() {
  const [msg, setMsg] = useState("");
  async function create(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    await post("/api/admin/categories", {
      name: fd.get("name"),
      description: fd.get("description"),
      parentId: fd.get("parentId") ? Number(fd.get("parentId")) : null,
      active: true
    });
    setMsg("Category saved");
  }
  return (
    <section>
      <h1>Categories</h1>
      <form className="form" onSubmit={create}>
        <input name="name" required placeholder="Name" />
        <input name="parentId" placeholder="Parent ID (optional)" />
        <textarea name="description" placeholder="Description" />
        <button className="btn" type="submit">Add category</button>
        {msg && <p className="success">{msg}</p>}
      </form>
    </section>
  );
}

export function AdminInventory() {
  const products = useQuery(() => get<{ content: ProductDetail[] }>("/api/admin/products?size=50"), []);
  const [msg, setMsg] = useState("");
  async function save(variantId: string, stockQty: number) {
    await post("/api/admin/inventory", { variantId, stockQty });
    setMsg("Inventory updated");
  }
  return (
    <section>
      <h1>Inventory</h1>
      {msg && <p className="success">{msg}</p>}
      <table className="table">
        <thead><tr><th>SKU</th><th>Variant</th><th>Available</th><th>Set qty</th></tr></thead>
        <tbody>
          {products.data?.content?.flatMap((p) => p.variants.map((v) => (
            <tr key={v.id}>
              <td>{v.sku}</td>
              <td>{p.name} / {v.name}</td>
              <td>{v.availableQty}</td>
              <td>
                <form onSubmit={(e) => { e.preventDefault(); const fd = new FormData(e.currentTarget); void save(v.id, Number(fd.get("qty"))); }}>
                  <input name="qty" type="number" defaultValue={v.stockQty} style={{ width: 80 }} />
                  <button className="btn secondary" type="submit">Save</button>
                </form>
              </td>
            </tr>
          )))}
        </tbody>
      </table>
    </section>
  );
}

export function AdminOrders() {
  const orders = useQuery(() => get<{ content: OrderView[] }>("/api/admin/orders"), []);
  const [msg, setMsg] = useState("");
  async function ship(id: string) {
    await patch(`/api/admin/orders/${id}`, { status: "SHIPPED", carrier: "VoltMart Logistics" });
    setMsg("Marked shipped");
  }
  async function deliver(id: string) {
    await patch(`/api/admin/orders/${id}`, { status: "DELIVERED" });
    setMsg("Marked delivered");
  }
  return (
    <section>
      <h1>Orders</h1>
      {msg && <p className="success">{msg}</p>}
      <table className="table">
        <thead><tr><th>Number</th><th>Status</th><th>Total</th><th></th></tr></thead>
        <tbody>
          {orders.data?.content?.map((o) => (
            <tr key={o.id}>
              <td>{o.orderNumber}</td>
              <td>{o.status}</td>
              <td>{money(o.total)}</td>
              <td>
                <button className="btn secondary" onClick={() => ship(o.id)}>Ship</button>
                <button className="btn ghost" onClick={() => deliver(o.id)}>Deliver</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

export function AdminReturns() {
  const list = useQuery(() => get<ReturnView[]>("/api/admin/returns"), []);
  async function resolve(id: string, approve: boolean) {
    await post(`/api/admin/returns/${id}/resolve`, { approve });
    window.location.reload();
  }
  return (
    <section>
      <h1>Returns</h1>
      <table className="table">
        <thead><tr><th>Order</th><th>Reason</th><th>Status</th><th></th></tr></thead>
        <tbody>
          {list.data?.map((r) => (
            <tr key={r.id}>
              <td>{r.orderNumber}</td>
              <td>{r.reason}</td>
              <td>{r.status}</td>
              <td>
                <button className="btn" onClick={() => resolve(r.id, true)}>Refund</button>
                <button className="btn danger" onClick={() => resolve(r.id, false)}>Reject</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

export function AdminBanners() {
  const [msg, setMsg] = useState("");
  async function create(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    await post("/api/admin/banners", {
      title: fd.get("title"),
      subtitle: fd.get("subtitle"),
      imageUrl: fd.get("imageUrl"),
      ctaLabel: fd.get("ctaLabel"),
      ctaUrl: fd.get("ctaUrl"),
      placement: fd.get("placement"),
      sortOrder: 0,
      active: true
    });
    setMsg("Banner saved");
  }
  return (
    <section>
      <h1>Banners</h1>
      <form className="form" onSubmit={create}>
        <input name="title" required placeholder="Title" />
        <input name="subtitle" placeholder="Subtitle" />
        <input name="imageUrl" required placeholder="Image URL" />
        <input name="ctaLabel" placeholder="CTA label" />
        <input name="ctaUrl" placeholder="CTA URL" />
        <select name="placement" defaultValue="HOME_HERO">
          <option>HOME_HERO</option>
          <option>HOME_STRIP</option>
        </select>
        <button className="btn" type="submit">Publish banner</button>
        {msg && <p className="success">{msg}</p>}
      </form>
    </section>
  );
}

export function AdminCmsPages() {
  const [msg, setMsg] = useState("");
  async function create(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    await post("/api/admin/pages", {
      slug: fd.get("slug"),
      title: fd.get("title"),
      body: fd.get("body"),
      published: true
    });
    setMsg("Page saved");
  }
  return (
    <section>
      <h1>CMS pages</h1>
      <form className="form" onSubmit={create}>
        <input name="slug" required placeholder="slug" />
        <input name="title" required placeholder="Title" />
        <textarea name="body" required placeholder="Body" rows={8} />
        <button className="btn" type="submit">Save page</button>
        {msg && <p className="success">{msg}</p>}
      </form>
    </section>
  );
}

export function AdminPromotions() {
  const [msg, setMsg] = useState("");
  async function coupon(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    await post("/api/admin/coupons", {
      code: fd.get("code"),
      description: fd.get("description"),
      discountType: fd.get("discountType"),
      amount: Number(fd.get("amount")),
      minOrder: Number(fd.get("minOrder") || 0),
      active: true
    });
    setMsg("Coupon saved");
  }
  async function promo(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    await post("/api/admin/promotions", {
      name: fd.get("name"),
      description: fd.get("description"),
      promoType: fd.get("promoType"),
      active: true
    });
    setMsg("Promotion saved");
  }
  return (
    <section>
      <h1>Promotions</h1>
      <form className="form" onSubmit={coupon}>
        <h3>Coupon</h3>
        <input name="code" required placeholder="CODE" />
        <input name="description" placeholder="Description" />
        <select name="discountType" defaultValue="PERCENT"><option>PERCENT</option><option>FIXED</option></select>
        <input name="amount" required type="number" step="0.01" />
        <input name="minOrder" type="number" step="0.01" placeholder="Min order" />
        <button className="btn" type="submit">Save coupon</button>
      </form>
      <form className="form" onSubmit={promo} style={{ marginTop: 24 }}>
        <h3>Promotion</h3>
        <input name="name" required placeholder="Name" />
        <input name="description" placeholder="Description" />
        <input name="promoType" defaultValue="FEATURED" />
        <button className="btn" type="submit">Save promotion</button>
      </form>
      {msg && <p className="success">{msg}</p>}
    </section>
  );
}

export function AdminCustomers() {
  const list = useQuery(() => get<CustomerView[]>("/api/admin/customers"), []);
  async function toggle(id: string, enabled: boolean) {
    await patch(`/api/admin/customers/${id}`, { enabled });
    window.location.reload();
  }
  return (
    <section>
      <h1>Customers</h1>
      <table className="table">
        <thead><tr><th>Email</th><th>Name</th><th>Roles</th><th></th></tr></thead>
        <tbody>
          {list.data?.map((c) => (
            <tr key={c.id}>
              <td>{c.email}</td>
              <td>{c.firstName} {c.lastName}</td>
              <td>{c.roles.join(", ")}</td>
              <td>
                <button className="btn secondary" onClick={() => toggle(c.id, !c.enabled)}>
                  {c.enabled ? "Disable" : "Enable"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

export function AdminReports() {
  const report = useQuery(() => get<ReportSummary>("/api/admin/reports/summary"), []);
  const audits = useQuery(() => get<{ content: AuditView[] }>("/api/admin/audit-logs"), []);
  return (
    <section>
      <h1>Reports & audit</h1>
      {report.data && <p>Today: {report.data.ordersToday} orders · {money(report.data.revenueToday)}</p>}
      <table className="table">
        <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Entity</th></tr></thead>
        <tbody>
          {audits.data?.content?.map((a) => (
            <tr key={a.id}>
              <td>{new Date(a.createdAt).toLocaleString()}</td>
              <td>{a.actorEmail}</td>
              <td>{a.action}</td>
              <td>{a.entityType} {a.entityId}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
