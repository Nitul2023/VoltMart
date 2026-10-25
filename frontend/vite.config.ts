import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 5173,
    allowedHosts: [".monkeycode-ai.live"],
    proxy: {
      "/api": {
        target: process.env.VITE_API_PROXY_TARGET || "http://127.0.0.1:8080",
        changeOrigin: true
      }
    }
  },
  preview: {
    host: "0.0.0.0",
    port: 5173,
    allowedHosts: [".monkeycode-ai.live"]
  },
  test: {
    environment: "jsdom",
    globals: true
  }
});
