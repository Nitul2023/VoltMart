import { FormEvent, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { del, get, post } from "../api/client";
import { ProductCardView } from "../components/ProductCardView";
import { useQuery } from "../hooks/useQuery";
import { useAuth } from "../store/auth";
import type { InvoiceView, OrderView, ProductCard, ShipmentView } from "../types";
import { money } from "../utils/money";

export function AccountHome() {
  const { user } = useAuth();
  const orders = useQuery(() => get<{ items: OrderView[] }>("/api/orders"), []);
  if (!user) {
    return (
      <div className="empty" style={{ margin: "40px 0" }}>
        <p>Sign in required.</p>
        <Link className="btn" to="/login">Sign in</Link>
      </div>
    );
  }
  return (
    <section>
      <h1>{user.firstName} {user.lastName}</h1>
      <p className="muted">{user.email}</p>
      <nav className="nav" style={{ margin: "16px 0" }}>
        <Link to="/account/wishlist">Wishlist</Link>
        <Link to="/account/orders">Orders</Link>
      </nav>
      <h2>Recent orders</h2>
      {!orders.loading && (!orders.data?.items || orders.data.items.length === 0) && (
        <div className="empty">
          <p>No orders yet.</p>
          <Link className="btn" to="/catalog">Start shopping</Link>
        </div>
      )}
      <table className="table">
        <thead><tr><th>Number</th><th>Status</th><th>Total</th></tr></thead>
        <tbody>
          {orders.data?.items.map((o) => (
            <tr key={o.id}>
              <td><Link to={`/account/orders/${o.orderNumber}`}>{o.orderNumber}</Link></td>
              <td>{o.status}</td>
              <td>{money(o.total)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

export function WishlistPage() {
  const list = useQuery(() => get<ProductCard[]>("/api/account/wishlist"), []);
  async function remove(id: string) {
    await del(`/api/account/wishlist/${id}`);
    window.location.reload();
  }
  return (
    <section>
      <h1>Wishlist</h1>
      {!list.loading && (!list.data || list.data.length === 0) && (
        <div className="empty">
          <p>Your wishlist is empty.</p>
          <Link className="btn" to="/catalog">Browse catalog</Link>
        </div>
      )}
      <div className="grid products">
        {list.data?.map((p) => (
          <div key={p.id}>
            <ProductCardView p={p} />
            <button className="btn secondary" onClick={() => remove(p.id)}>Remove</button>
          </div>
        ))}
      </div>
    </section>
  );
}

export function OrderDetailPage() {
  const { orderNumber = "" } = useParams();
  const order = useQuery(() => get<OrderView>(`/api/orders/${orderNumber}`), [orderNumber]);
  const tracking = useQuery(() => get<ShipmentView[]>(`/api/orders/${orderNumber}/tracking`), [orderNumber]);
  const [invoice, setInvoice] = useState<InvoiceView | null>(null);
  const [msg, setMsg] = useState("");
  const o = order.data;
  if (order.loading) return <p>Loading order…</p>;
  if (!o) return <p className="error" role="alert">{order.error || "Not found"}</p>;

  async function cancel() {
    await post(`/api/orders/${orderNumber}/cancel`, { reason: "Changed mind" });
    setMsg("Cancellation requested");
  }
  async function ret() {
    await post(`/api/orders/${orderNumber}/returns`, { reason: "Not as described" });
    setMsg("Return requested");
  }
  async function showInvoice() {
    setInvoice(await get<InvoiceView>(`/api/orders/${orderNumber}/invoice`));
  }

  return (
    <article>
      <h1>Order {o.orderNumber}</h1>
      <p>Status <strong>{o.status}</strong> · {new Date(o.createdAt).toLocaleString()}</p>
      <p>{o.shippingName}<br />{o.shippingLine1}<br />{o.shippingCity}, {o.shippingState} {o.shippingPostal}</p>
      <table className="table">
        <tbody>
          {o.items.map((i) => (
            <tr key={i.id}><td>{i.productName} / {i.variantName}</td><td>{i.quantity}</td><td>{money(i.lineTotal)}</td></tr>
          ))}
        </tbody>
      </table>
      <p>Subtotal {money(o.subtotal)} · Discount {money(o.discount)} · Shipping {money(o.shippingCost)} · Tax {money(o.tax)}</p>
      <p className="price">Total {money(o.total)}</p>
      {o.payment && <p>Payment {o.payment.status} {o.payment.brand} •••• {o.payment.last4} {o.payment.verified ? "(verified)" : ""}</p>}
      {tracking.data?.map((s) => (
        <p key={s.id}>Tracking {s.carrier} {s.trackingNumber} · {s.status}</p>
      ))}
      <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
        {["PAID", "PROCESSING", "PENDING_PAYMENT"].includes(o.status) && <button className="btn danger" onClick={cancel}>Cancel & refund</button>}
        {["SHIPPED", "DELIVERED"].includes(o.status) && <button className="btn secondary" onClick={ret}>Request return</button>}
        <button className="btn ghost" onClick={showInvoice}>View invoice</button>
      </div>
      {msg && <p className="success" role="status">{msg}</p>}
      {invoice && <div dangerouslySetInnerHTML={{ __html: invoice.html || "" }} />}
    </article>
  );
}

export function CmsPageView() {
  const { slug = "" } = useParams();
  const page = useQuery(() => get<{ title: string; body: string }>(`/api/pages/${slug}`), [slug]);
  useEffect(() => {
    if (page.data?.title) document.title = `${page.data.title} | VoltMart`;
    return () => { document.title = "VoltMart | Electronics"; };
  }, [page.data?.title]);
  if (page.loading) return <p>Loading…</p>;
  if (!page.data) return <p className="error" role="alert">{page.error || "Not found"}</p>;
  return (
    <article>
      <h1>{page.data.title}</h1>
      <p style={{ maxWidth: 720 }}>{page.data.body}</p>
    </article>
  );
}

export function ReviewForm({ productId }: { productId: string }) {
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    const fd = new FormData(e.currentTarget);
    try {
      await post(`/api/products/${productId}/reviews`, {
        rating: Number(fd.get("rating")),
        title: fd.get("title"),
        body: fd.get("body")
      });
      setMsg("Thanks for the review");
    } catch (err) {
      setError((err as Error).message);
    }
  }
  return (
    <form className="form" onSubmit={submit}>
      <label>Rating
        <select name="rating" defaultValue="5"><option>5</option><option>4</option><option>3</option><option>2</option><option>1</option></select>
      </label>
      <label>Title
        <input name="title" placeholder="Title" />
      </label>
      <label>Review
        <textarea name="body" required />
      </label>
      <button className="btn" type="submit">Submit review</button>
      {msg && <p className="success" role="status">{msg}</p>}
      {error && <p className="error" role="alert">{error}</p>}
    </form>
  );
}
