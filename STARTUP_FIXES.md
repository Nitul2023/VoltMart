# VoltMart — fixes for local startup

## Issue 1: Vite `/api/...` proxy errors (`ECONNREFUSED`)

The frontend is trying to reach the backend at `http://127.0.0.1:8080`. These errors happen while the Spring Boot API is not listening, so start the backend successfully first (steps below), then restart Vite. The proxy target can be overridden with `VITE_API_PROXY_TARGET` if your API uses another host/port.

Terminal 1 (backend):
```powershell
cd workspaceackend
mvn spring-boot:run
```

Terminal 2 (frontend):
```powershell
cd workspacerontend
npm install
npm run dev
```

Open http://localhost:5173/.

## Issue 2: Spring Boot fails with Flyway `Migration checksum mismatch for migration version 2`

Your log shows PostgreSQL is reachable, but Flyway refuses to start because the version-2 migration file differs from the version previously applied to database `electroshop`. Do not delete the database or `flyway_schema_history` table.

If the current `V2__seed_data.sql` is the version you intend to keep and its changes have already been applied manually / are safe for your database, repair the Flyway checksum using the same database credentials as `application.yml`:

```powershell
cd workspaceackend
mvn org.flywaydb:flyway-maven-plugin:9.22.3:repair `
  "-Dflyway.url=jdbc:postgresql://localhost:5432/electroshop" `
  "-Dflyway.user=electroshop" `
  "-Dflyway.password=electroshop"
```

Replace the username/password with your actual database credentials if they differ. This updates Flyway's recorded migration checksum; it does not rerun V2 or change application tables. Back up your database first. Only run repair if you have reviewed the changed migration and intend to accept the current file. If you need to preserve the exact original migration history, restore the original V2 file instead of repairing.

Then start the backend:
```powershell
mvn spring-boot:run
```

If the repair command cannot connect, confirm PostgreSQL is running and that the database name, username, password, and port match `application.yml`.

## npm audit notice

The `whatwg-encoding` deprecation message is a warning, not the cause of the API proxy errors. The 8 reported npm vulnerabilities are a separate dependency audit result. Review them with `npm audit`; avoid `npm audit fix --force` unless you are prepared for potentially breaking dependency upgrades.
