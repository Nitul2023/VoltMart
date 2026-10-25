import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { post } from "../api/client";
import { useAuth } from "../store/auth";
import type { AuthResponse } from "../types";

export function LoginPage() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [show, setShow] = useState(false);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setBusy(true);
    try {
      const res = await post<AuthResponse>("/api/auth/login", {
        email: fd.get("email"),
        password: fd.get("password")
      });
      login(res.token, res.user);
      nav(res.user.roles.some((r) => r !== "CUSTOMER") ? "/admin" : "/account");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="form" style={{ maxWidth: 420, margin: "40px auto" }} onSubmit={submit}>
      <h1>Sign in</h1>
      <label>Email
        <input name="email" type="email" required autoComplete="email" defaultValue="jane@example.com" />
      </label>
      <label>Password
        <input name="password" type={show ? "text" : "password"} required autoComplete="current-password" defaultValue="password" />
      </label>
      <button type="button" className="btn ghost" onClick={() => setShow((s) => !s)}>{show ? "Hide password" : "Show password"}</button>
      {error && <p className="error" role="alert">{error}</p>}
      <button className="btn" type="submit" disabled={busy}>{busy ? "Signing in…" : "Continue"}</button>
      <p className="muted">No account? <Link className="create-account-link" to="/register">Create one</Link>. Staff: admin@voltmart.local / password</p>
    </form>
  );
}

export function RegisterPage() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setBusy(true);
    try {
      const res = await post<AuthResponse>("/api/auth/register", {
        email: fd.get("email"),
        password: fd.get("password"),
        firstName: fd.get("firstName"),
        lastName: fd.get("lastName"),
        phone: fd.get("phone")
      });
      login(res.token, res.user);
      nav("/account");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="form" style={{ maxWidth: 420, margin: "40px auto" }} onSubmit={submit}>
      <h1>Create account</h1>
      <label>First name
        <input name="firstName" required autoComplete="given-name" />
      </label>
      <label>Last name
        <input name="lastName" required autoComplete="family-name" />
      </label>
      <label>Email
        <input name="email" type="email" required autoComplete="email" />
      </label>
      <label>Phone
        <input name="phone" type="tel" autoComplete="tel" />
      </label>
      <label>Password (8+ characters)
        <input name="password" type="password" required minLength={8} autoComplete="new-password" />
      </label>
      {error && <p className="error" role="alert">{error}</p>}
      <button className="btn" type="submit" disabled={busy}>{busy ? "Creating…" : "Register"}</button>
    </form>
  );
}
