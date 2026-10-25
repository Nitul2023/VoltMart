# VoltMart fixes applied

## Product creation and CMS product list
- Product SKU is trimmed and checked for duplicates before persistence.
- Product slugs are generated from product name + SKU, capped to a safe length, and made unique when generated. A duplicate user-supplied slug returns a clear conflict response.
- Product and variant validation now rejects invalid or duplicate SKU/variant values with useful HTTP responses instead of a generic database 500 where the error is a known constraint issue.
- Successful product creation refreshes the saved product list (existing behavior retained).

## Delete product button
- Added a Delete action to each row in CMS > Products.
- Delete is a soft delete: it marks a product inactive so it no longer appears in the active CMS list/storefront while preserving historical order references.
- Deletion requires ADMIN or MANAGER privileges and is audited.

## Frontend warnings
- Opted into the React Router v7 future flags. Test navigation, especially splat/nested routes, before production deployment.
- Added a small SVG favicon and referenced it from index.html.

## Run locally
1. Start PostgreSQL and ensure backend application properties point to the correct database.
2. In `backend`, run `mvn spring-boot:run` (port 8080 must be free).
3. In `frontend`, run `npm install`, then `npm run dev`.
4. Open the Vite URL shown in the terminal and sign in with an account authorized for CMS product management.

## Notes
- Do not run `npm audit fix --force` blindly; review the proposed breaking dependency changes first.
- The frontend TypeScript check passed during preparation. A full Vite build could not be verified in this environment because the extracted dependency tree lacks the platform-specific Rollup optional binary. Run `npm install` on your machine before building. The backend could not be compiled here because Maven is not installed in the preparation environment.
- If product creation still returns 500, use the backend terminal stack trace for the specific cause; this package improves common uniqueness/validation failures but cannot validate against your local database.
