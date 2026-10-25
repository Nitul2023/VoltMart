
const SESSION_KEY = "vm_cart_session";
const TOKEN_KEY = "vm_token";

const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ?? ""
).replace(/\/+$/, "");

export function cartSession(): string {
  let id = localStorage.getItem(SESSION_KEY);

  if (!id) {
    id = crypto.randomUUID();
    localStorage.setItem(SESSION_KEY, id);
  }

  return id;
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }

  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("vm-auth"));
  }
}

export async function api<T>(
  path: string,
  init: RequestInit = {}
): Promise<T> {
  const headers = new Headers(init.headers);

  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }

  const token = getToken();

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  headers.set("X-Cart-Session", cartSession());

  const url = `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;

  const res = await fetch(url, {
    ...init,
    headers,
  });

  if (res.status === 204) {
    return undefined as T;
  }

  const text = await res.text();
  let data: any = null;

  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      if (!res.ok) {
        throw new Error(
          `API request failed (${res.status}). Check the backend URL and server logs.`
        );
      }

      throw new Error(
        "The API returned a non-JSON response. Check VITE_API_BASE_URL and the backend endpoint."
      );
    }
  }

  if (!res.ok) {
    const message =
      data?.message || data?.error || res.statusText || "Request failed";

    throw new Error(message);
  }

  return data as T;
}

export const get = <T>(path: string) => api<T>(path);

export const post = <T>(path: string, body?: unknown) =>
  api<T>(path, {
    method: "POST",
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

export const put = <T>(path: string, body: unknown) =>
  api<T>(path, {
    method: "PUT",
    body: JSON.stringify(body),
  });

export const patch = <T>(path: string, body: unknown) =>
  api<T>(path, {
    method: "PATCH",
    body: JSON.stringify(body),
  });

export const del = <T>(path: string) =>
  api<T>(path, { method: "DELETE" });
