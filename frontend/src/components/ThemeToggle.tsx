import { useTheme } from "../store/theme";

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  const nextTheme = theme === "dark" ? "light" : "dark";
  return (
    <button className="btn secondary theme-toggle" type="button" onClick={toggleTheme} aria-label={`Switch to ${nextTheme} mode`} title={`Switch to ${nextTheme} mode`}>
      <span aria-hidden="true">{theme === "dark" ? "☀" : "☾"}</span>
      {theme === "dark" ? "Light mode" : "Dark mode"}
    </button>
  );
}
