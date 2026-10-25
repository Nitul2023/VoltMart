import { describe, expect, it } from "vitest";
import { availabilityLabel, estimateCheckout, money } from "./money";

describe("money", () => {
  it("formats Indian rupees", () => {
    expect(money(12.5)).toContain("12.50");
    expect(money(12.5)).toContain("₹");
  });
  it("maps availability", () => {
    expect(availabilityLabel("IN_STOCK")).toBe("In stock");
    expect(availabilityLabel("OUT_OF_STOCK")).toBe("Out of stock");
  });
  it("applies free ground shipping over 999", () => {
    const e = estimateCheckout(1249, "GROUND", 24.9);
    expect(e.shipping).toBe(0);
    expect(e.total).toBeGreaterThan(0);
  });
});
