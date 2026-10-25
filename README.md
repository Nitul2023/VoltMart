# VoltMart

Electronics e-commerce platform with a React + TypeScript storefront, a Spring Boot REST API, PostgreSQL, and an admin CMS.

## Stack

- Frontend: React 18, TypeScript, Vite
- Backend: Java 17, Spring Boot 3.2, Spring Security, JWT, JPA, Flyway
- Database: PostgreSQL 15
- Tests: JUnit 5, MockMvc, Vitest
- Deploy: Docker Compose

## Features

Storefront: homepage banners, catalog, category tree, brand filters, search (name / SKU / category), autocomplete, sort, pagination, category filters, product images and variants, specs, pricing and discounts, warranty, stock, reviews, comparison, wishlist, cart, checkout, coupons, payments, shipping, tracking, invoices, cancel, returns, refunds, accounts.

Admin CMS: products, categories, inventory, orders, banners, pages, promotions, customers, reports, audit logs. Role-based access (ADMIN, MANAGER, EDITOR, SUPPORT, CUSTOMER).

## Demo accounts

Password for all seeded users: `password`

- admin@voltmart.local (ADMIN)
- manager@voltmart.local (MANAGER)
- support@voltmart.local (SUPPORT)
- editor@voltmart.local (EDITOR)
- jane@example.com (CUSTOMER)

Coupon: `WELCOME10` (10% off, min $50). Test card: `4242424242424242`. Decline card: any number ending in `0000`.

## Local run

PostgreSQL must be available (`electroshop` / `electroshop` on port 5432).

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend (proxies /api to :8080)
cd frontend
npm install
npm run dev
```

Storefront: http://localhost:5173  
Admin CMS: http://localhost:5173/admin  
API health: http://localhost:8080/api/health

## Tests

```bash
cd backend && mvn test
cd frontend && npm test
```

## Docker

```bash
docker compose up --build
```
