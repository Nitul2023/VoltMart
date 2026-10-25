export function money(n?: number | null) {
  const v = Number(n || 0);
  return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(v);
}

export function availabilityLabel(code: string) {
  if (code === "IN_STOCK") return "In stock";
  if (code === "LOW_STOCK") return "Low stock";
  return "Out of stock";
}

export function estimateCheckout(subtotal: number, method = "GROUND", discount = 0) {
  const afterDiscount = Math.max(0, subtotal - discount);
  let shipping = 99;
  if (method === "EXPRESS") shipping = 199;
  else if (method === "OVERNIGHT") shipping = 299;
  else if (afterDiscount >= 999) shipping = 0;
  const tax = Math.round((afterDiscount + shipping) * 0.18 * 100) / 100;
  const total = Math.round((afterDiscount + shipping + tax) * 100) / 100;
  return { shipping, tax, total, afterDiscount };
}
