import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { get, post } from "../api/client";
import { useAuth } from "../store/auth";
import { useCart } from "../store/cart";
import type { CouponPreview, OrderView } from "../types";
import { estimateCheckout, money } from "../utils/money";

export function CheckoutPage() {
  const { user } = useAuth();
  const { cart, refresh } = useCart();
  const nav = useNavigate();
  const [error, setError] = useState("");
  const [coupon, setCoupon] = useState("");
  const [preview, setPreview] = useState<CouponPreview | null>(null);
  const [method, setMethod] = useState("GROUND");
  const [busy, setBusy] = useState(false);

  if (!user) {
    return (
      <div className="empty" style={{ margin: "40px 0" }}>
        <h1>Sign in to checkout</h1>
        <p className="muted">Your cart is saved — continue after signing in.</p>
        <Link className="btn" to="/login">Sign in</Link>
      </div>
    );
  }
  if (!cart || cart.items.length === 0) {
    return (
      <div className="empty" style={{ margin: "40px 0" }}>
        <p>Cart is empty.</p>
        <Link className="btn" to="/catalog">Browse catalog</Link>
      </div>
    );
  }

  const discount = preview?.valid ? preview.discount : 0;
  const est = estimateCheckout(cart.subtotal, method, discount);

  async function applyCoupon() {
    const p = await get<CouponPreview>(`/api/coupons/preview?code=${encodeURIComponent(coupon)}&subtotal=${cart!.subtotal}`);
    setPreview(p);
  }

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    setBusy(true);
    const fd = new FormData(e.currentTarget);
    try {
      const order = await post<OrderView>("/api/checkout", {
        shipping: {
          label: "Checkout",
          line1: fd.get("line1"),
          line2: fd.get("line2"),
          city: fd.get("city"),
          state: fd.get("state"),
          postalCode: fd.get("postalCode"),
          country: "United States",
          defaultAddress: true
        },
        shippingMethod: fd.get("shippingMethod"),
        couponCode: coupon || null,
        paymentMethod: "CARD",
        cardNumber: String(fd.get("cardNumber")).replace(/\s/g, ""),
        cardExpiry: fd.get("cardExpiry"),
        cardCvc: fd.get("cardCvc"),
        notes: fd.get("notes")
      });
      await refresh();
      nav(`/account/orders/${order.orderNumber}`);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="layout">
      <form className="form" onSubmit={submit}>
        <h1>Secure checkout</h1>
        <label>Address line 1
          <input name="line1" required autoComplete="address-line1" defaultValue="128 Market Street" />
        </label>
        <label>Address line 2
          <input name="line2" autoComplete="address-line2" />
        </label>
        <label>City
          <input name="city" required autoComplete="address-level2" defaultValue="Guwahati" />
        </label>
        <label>State
          <input name="state" required autoComplete="address-level1" defaultValue="Assam" />
        </label>
        <label>Postal code
          <input name="postalCode" required autoComplete="postal-code" defaultValue="781001" />
        </label>
        <label>Shipping
          <select name="shippingMethod" value={method} onChange={(e) => setMethod(e.target.value)}>
            <option value="GROUND">Standard (free over ₹999)</option>
            <option value="EXPRESS">Express ₹199</option>
            <option value="OVERNIGHT">Overnight ₹299</option>
          </select>
        </label>
        <div>
          <label>Coupon code
            <input value={coupon} onChange={(e) => setCoupon(e.target.value)} placeholder="WELCOME10" />
          </label>
          <button type="button" className="btn secondary" onClick={applyCoupon} style={{ marginTop: 8 }}>Apply</button>
          {preview && <p className={preview.valid ? "success" : "error"} role="status">{preview.message} {preview.valid && money(preview.discount)}</p>}
        </div>
        <h3>Payment</h3>
        <label>Card number
          <input name="cardNumber" required inputMode="numeric" autoComplete="cc-number" defaultValue="4242424242424242" />
        </label>
        <label>Expiry
          <input name="cardExpiry" required placeholder="MM/YY" autoComplete="cc-exp" defaultValue="12/29" />
        </label>
        <label>CVC
          <input name="cardCvc" required autoComplete="cc-csc" defaultValue="123" />
        </label>
        <label>Delivery notes
          <textarea name="notes" />
        </label>
        {error && <p className="error" role="alert">{error}</p>}
        <button className="btn" type="submit" disabled={busy}>{busy ? "Processing…" : `Pay ${money(est.total)}`}</button>
      </form>
      <aside className="filters">
        <h3>Order summary</h3>
        {cart.items.map((i) => (
          <p key={i.id}>{i.productName} × {i.quantity}<br /><span className="muted">{money(i.lineTotal)}</span></p>
        ))}
        <p>Subtotal {money(cart.subtotal)}</p>
        {discount > 0 && <p>Discount −{money(discount)}</p>}
        <p>Shipping {est.shipping === 0 ? "Free" : money(est.shipping)}</p>
        <p>Tax {money(est.tax)}</p>
        <p className="price">Total {money(est.total)}</p>
      </aside>
    </section>
  );
}
