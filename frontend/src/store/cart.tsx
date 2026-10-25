import { createContext, useContext, useEffect, useState } from "react";
import type { CartView } from "../types";
import { del, get, patch, post } from "../api/client";

interface CartState {
  cart: CartView | null;
  refresh: () => Promise<void>;
  add: (variantId: string, quantity?: number) => Promise<void>;
  updateQty: (itemId: string, quantity: number) => Promise<void>;
  clear: () => Promise<void>;
}

const Ctx = createContext<CartState | undefined>(undefined);

const empty: CartView = { id: "", items: [], subtotal: 0, itemCount: 0 };

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [cart, setCart] = useState<CartView | null>(null);

  const refresh = async () => {
    try {
      setCart(await get<CartView>("/api/cart"));
    } catch {
      setCart(empty);
    }
  };

  useEffect(() => { void refresh(); }, []);
  useEffect(() => {
    const onAuth = () => { void refresh(); };
    window.addEventListener("vm-auth", onAuth);
    return () => window.removeEventListener("vm-auth", onAuth);
  }, []);

  return (
    <Ctx.Provider value={{
      cart,
      refresh,
      add: async (variantId, quantity = 1) => {
        setCart(await post<CartView>("/api/cart/items", { variantId, quantity }));
      },
      updateQty: async (itemId, quantity) => {
        setCart(await patch<CartView>(`/api/cart/items/${itemId}`, { quantity }));
      },
      clear: async () => {
        setCart(await del<CartView>("/api/cart"));
      }
    }}>{children}</Ctx.Provider>
  );
}

export function useCart() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("CartProvider missing");
  return ctx;
}
