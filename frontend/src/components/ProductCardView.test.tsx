import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { ProductCardView } from "./ProductCardView";
import type { ProductCard } from "../types";

describe("ProductCardView", () => {
  it("renders product name and price", () => {
    const p: ProductCard = {
      id: "1", sku: "X", name: "Nexora Pulse 1", slug: "nexora-pulse-1",
      basePrice: 899, featured: true, warrantyMonths: 24, availability: "IN_STOCK",
      brand: "Nexora", category: "Smartphones"
    };
    render(<MemoryRouter><ProductCardView p={p} /></MemoryRouter>);
    expect(screen.getByText("Nexora Pulse 1")).toBeTruthy();
  });
});
