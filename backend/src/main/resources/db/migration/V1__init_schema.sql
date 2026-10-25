CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(180) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(80) NOT NULL,
    last_name       VARCHAR(80) NOT NULL,
    phone           VARCHAR(40),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id         BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE addresses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label           VARCHAR(60),
    line1           VARCHAR(160) NOT NULL,
    line2           VARCHAR(160),
    city            VARCHAR(80) NOT NULL,
    state           VARCHAR(80) NOT NULL,
    postal_code     VARCHAR(20) NOT NULL,
    country         VARCHAR(80) NOT NULL DEFAULT 'United States',
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    name            VARCHAR(120) NOT NULL,
    slug            VARCHAR(140) NOT NULL UNIQUE,
    description     TEXT,
    image_url       VARCHAR(500),
    sort_order      INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    seo_title       VARCHAR(180),
    seo_description VARCHAR(320)
);

CREATE INDEX idx_categories_parent ON categories(parent_id);

CREATE TABLE brands (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL UNIQUE,
    slug            VARCHAR(140) NOT NULL UNIQUE,
    logo_url        VARCHAR(500),
    description     TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE filter_definitions (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name            VARCHAR(80) NOT NULL,
    slug            VARCHAR(100) NOT NULL,
    filter_type     VARCHAR(20) NOT NULL,
    unit            VARCHAR(30),
    sort_order      INT NOT NULL DEFAULT 0,
    UNIQUE (category_id, slug)
);

CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku             VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(220) NOT NULL,
    slug            VARCHAR(240) NOT NULL UNIQUE,
    short_description VARCHAR(400),
    description     TEXT,
    category_id     BIGINT NOT NULL REFERENCES categories(id),
    brand_id        BIGINT NOT NULL REFERENCES brands(id),
    base_price      NUMERIC(12,2) NOT NULL,
    compare_at_price NUMERIC(12,2),
    discount_percent NUMERIC(5,2),
    warranty_months INT NOT NULL DEFAULT 12,
    weight_grams    INT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    featured        BOOLEAN NOT NULL DEFAULT FALSE,
    seo_title       VARCHAR(180),
    seo_description VARCHAR(320),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_name ON products(LOWER(name));
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_active ON products(active);

CREATE TABLE product_images (
    id              BIGSERIAL PRIMARY KEY,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url             VARCHAR(500) NOT NULL,
    alt_text        VARCHAR(180),
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE TABLE product_variants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku             VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(160) NOT NULL,
    color           VARCHAR(60),
    storage         VARCHAR(40),
    other_option    VARCHAR(80),
    price           NUMERIC(12,2) NOT NULL,
    compare_at_price NUMERIC(12,2),
    stock_qty       INT NOT NULL DEFAULT 0,
    reserved_qty    INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_variants_product ON product_variants(product_id);

CREATE TABLE product_specs (
    id              BIGSERIAL PRIMARY KEY,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    group_name      VARCHAR(80) NOT NULL,
    spec_key        VARCHAR(120) NOT NULL,
    spec_value      VARCHAR(240) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0
);

CREATE TABLE product_filter_values (
    id              BIGSERIAL PRIMARY KEY,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    filter_id       BIGINT NOT NULL REFERENCES filter_definitions(id) ON DELETE CASCADE,
    value_text      VARCHAR(160) NOT NULL,
    UNIQUE (product_id, filter_id)
);

CREATE TABLE reviews (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating          INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title           VARCHAR(160),
    body            TEXT NOT NULL,
    verified        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (product_id, user_id)
);

CREATE INDEX idx_reviews_product ON reviews(product_id);

CREATE TABLE wishlists (
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    added_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, product_id)
);

CREATE TABLE carts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    session_id      VARCHAR(80) UNIQUE,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cart_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id         UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id      UUID NOT NULL REFERENCES product_variants(id),
    quantity        INT NOT NULL CHECK (quantity > 0),
    UNIQUE (cart_id, variant_id)
);

CREATE TABLE coupons (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(40) NOT NULL UNIQUE,
    description     VARCHAR(200),
    discount_type   VARCHAR(20) NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    min_order       NUMERIC(12,2) NOT NULL DEFAULT 0,
    max_uses        INT,
    used_count      INT NOT NULL DEFAULT 0,
    starts_at       TIMESTAMPTZ,
    ends_at         TIMESTAMPTZ,
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number    VARCHAR(24) NOT NULL UNIQUE,
    user_id         UUID NOT NULL REFERENCES users(id),
    status          VARCHAR(30) NOT NULL,
    subtotal        NUMERIC(12,2) NOT NULL,
    discount        NUMERIC(12,2) NOT NULL DEFAULT 0,
    shipping_cost   NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax             NUMERIC(12,2) NOT NULL DEFAULT 0,
    total           NUMERIC(12,2) NOT NULL,
    coupon_code     VARCHAR(40),
    shipping_name   VARCHAR(160) NOT NULL,
    shipping_line1  VARCHAR(160) NOT NULL,
    shipping_line2  VARCHAR(160),
    shipping_city   VARCHAR(80) NOT NULL,
    shipping_state  VARCHAR(80) NOT NULL,
    shipping_postal VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(80) NOT NULL,
    shipping_method VARCHAR(40) NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cancelled_at    TIMESTAMPTZ,
    cancel_reason   VARCHAR(240)
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at DESC);

CREATE TABLE order_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id      UUID NOT NULL,
    variant_id      UUID NOT NULL,
    sku             VARCHAR(64) NOT NULL,
    product_name    VARCHAR(220) NOT NULL,
    variant_name    VARCHAR(160) NOT NULL,
    unit_price      NUMERIC(12,2) NOT NULL,
    quantity        INT NOT NULL,
    line_total      NUMERIC(12,2) NOT NULL
);

CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id),
    provider        VARCHAR(40) NOT NULL,
    method          VARCHAR(40) NOT NULL,
    status          VARCHAR(30) NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    transaction_ref VARCHAR(80) UNIQUE,
    last4           VARCHAR(4),
    brand           VARCHAR(40),
    verified        BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason  VARCHAR(240),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE shipments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id),
    carrier         VARCHAR(60) NOT NULL,
    tracking_number VARCHAR(80) UNIQUE,
    status          VARCHAR(30) NOT NULL,
    shipped_at      TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL UNIQUE REFERENCES orders(id),
    invoice_number  VARCHAR(24) NOT NULL UNIQUE,
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    pdf_url         VARCHAR(500)
);

CREATE TABLE returns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    reason          VARCHAR(240) NOT NULL,
    status          VARCHAR(30) NOT NULL,
    refund_amount   NUMERIC(12,2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMPTZ
);

CREATE TABLE refunds (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_id       UUID REFERENCES returns(id),
    order_id        UUID NOT NULL REFERENCES orders(id),
    amount          NUMERIC(12,2) NOT NULL,
    status          VARCHAR(30) NOT NULL,
    transaction_ref VARCHAR(80),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE banners (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(160) NOT NULL,
    subtitle        VARCHAR(240),
    image_url       VARCHAR(500) NOT NULL,
    cta_label       VARCHAR(60),
    cta_url         VARCHAR(240),
    placement       VARCHAR(40) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at       TIMESTAMPTZ,
    ends_at         TIMESTAMPTZ
);

CREATE TABLE cms_pages (
    id              BIGSERIAL PRIMARY KEY,
    slug            VARCHAR(140) NOT NULL UNIQUE,
    title           VARCHAR(180) NOT NULL,
    body            TEXT NOT NULL,
    seo_title       VARCHAR(180),
    seo_description VARCHAR(320),
    published       BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE promotions (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(160) NOT NULL,
    description     TEXT,
    promo_type      VARCHAR(40) NOT NULL,
    promo_value     NUMERIC(12,2),
    product_id      UUID REFERENCES products(id) ON DELETE CASCADE,
    category_id     BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at       TIMESTAMPTZ,
    ends_at         TIMESTAMPTZ
);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    actor_id        UUID,
    actor_email     VARCHAR(180),
    action          VARCHAR(80) NOT NULL,
    entity_type     VARCHAR(80) NOT NULL,
    entity_id       VARCHAR(80),
    details         TEXT,
    ip_address      VARCHAR(60),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);

CREATE TABLE email_notifications (
    id              BIGSERIAL PRIMARY KEY,
    recipient       VARCHAR(180) NOT NULL,
    subject         VARCHAR(220) NOT NULL,
    body            TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL,
    related_type    VARCHAR(40),
    related_id      VARCHAR(80),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE analytics_events (
    id              BIGSERIAL PRIMARY KEY,
    event_name      VARCHAR(80) NOT NULL,
    user_id         UUID,
    session_id      VARCHAR(80),
    path            VARCHAR(240),
    metadata        TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_analytics_created ON analytics_events(created_at DESC);
