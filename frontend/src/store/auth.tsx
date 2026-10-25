import { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { UserView } from "../types";
import { get, getToken, setToken } from "../api/client";

interface AuthState {
  user: UserView | null;
  token: string | null;
  loading: boolean;
  login: (token: string, user: UserView) => void;
  logout: () => void;
  isStaff: boolean;
}

const Ctx = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserView | null>(null);
  const [token, setTok] = useState<string | null>(getToken());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }
    get<UserView>("/api/auth/me")
      .then(setUser)
      .catch(() => {
        setToken(null);
        setTok(null);
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, [token]);

  const value = useMemo<AuthState>(() => ({
    user,
    token,
    loading,
    login: (t, u) => {
      setToken(t);
      setTok(t);
      setUser(u);
    },
    logout: () => {
      setToken(null);
      setTok(null);
      setUser(null);
    },
    isStaff: !!user?.roles.some((r) => ["ADMIN", "MANAGER", "EDITOR", "SUPPORT"].includes(r))
  }), [user, token, loading]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useAuth() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("AuthProvider missing");
  return ctx;
}
