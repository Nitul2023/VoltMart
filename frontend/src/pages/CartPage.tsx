import { Link } from "react-router-dom";
import { useCart } from "../store/cart";
import { money } from "../utils/money";

export function CartPage() {
  const { cart, updateQty } = useCart();
  if (!cart || cart.items.length === 0) {
    return (
      <div className="empty" style={{ margin: "40px 0" }}>
        <h1>Your cart is empty</h1>
        <p className="muted">Add a laptop, phone, or headphones to get started.</p>
        <Link className="btn" to="/catalog">Browse catalog</Link>
      </div>
    );
  }
  return (
    <section>
      <h1>Cart</h1>
      <table className="table">
        <thead><tr><th>Item</th><th>Qty</th><th>Price</th></tr></thead>
        <tbody>
          {cart.items.map((i) => (
            <tr key={i.id}>
              <td>
                <strong>{i.productName}</strong>
                <div className="muted">{i.variantName} · {i.sku}</div>
              </td>
              <td>
                <label className="muted">
                  Qty
                  <input type="number" min={0} max={i.availableQty} value={i.quantity} aria-label={`Quantity for ${i.productName}`}
                    onChange={(e) => updateQty(i.id, Number(e.target.value))} style={{ width: 72, marginLeft: 8 }} />
                </label>
              </td>
              <td>{money(i.lineTotal)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <p className="price">Subtotal {money(cart.subtotal)}</p>
      <Link className="btn" to="/checkout">Checkout</Link>
    </section>
  );
}
