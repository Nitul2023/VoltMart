INSERT INTO roles (name) VALUES
    ('CUSTOMER'), ('ADMIN'), ('MANAGER'), ('SUPPORT'), ('EDITOR');

INSERT INTO users (id, email, password_hash, first_name, last_name, phone, enabled, email_verified)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin@voltmart.local',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'Ava', 'Chen', '+1-555-0100', TRUE, TRUE),
    ('22222222-2222-2222-2222-222222222222', 'manager@voltmart.local',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'Marcus', 'Reed', '+1-555-0101', TRUE, TRUE),
    ('33333333-3333-3333-3333-333333333333', 'support@voltmart.local',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'Sofia', 'Patel', '+1-555-0102', TRUE, TRUE),
    ('44444444-4444-4444-4444-444444444444', 'editor@voltmart.local',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'Leo', 'Nakamura', '+1-555-0103', TRUE, TRUE),
    ('55555555-5555-5555-5555-555555555555', 'jane@example.com',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
     'Jane', 'Doe', '+1-555-0199', TRUE, TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT '11111111-1111-1111-1111-111111111111', id FROM roles WHERE name = 'ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT '22222222-2222-2222-2222-222222222222', id FROM roles WHERE name = 'MANAGER';
INSERT INTO user_roles (user_id, role_id)
SELECT '33333333-3333-3333-3333-333333333333', id FROM roles WHERE name = 'SUPPORT';
INSERT INTO user_roles (user_id, role_id)
SELECT '44444444-4444-4444-4444-444444444444', id FROM roles WHERE name = 'EDITOR';
INSERT INTO user_roles (user_id, role_id)
SELECT '55555555-5555-5555-5555-555555555555', id FROM roles WHERE name = 'CUSTOMER';

INSERT INTO addresses (user_id, label, line1, city, state, postal_code, country, is_default)
VALUES ('55555555-5555-5555-5555-555555555555', 'Home', '128 Market Street', 'Austin', 'TX', '78701', 'United States', TRUE);

INSERT INTO categories (id, parent_id, name, slug, description, image_url, sort_order, seo_title, seo_description) VALUES
    (1, NULL, 'Smartphones', 'smartphones', 'Flagship and mid-range mobile phones.', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800', 1, 'Smartphones | VoltMart', 'Shop the latest smartphones with warranty and fast shipping.'),
    (2, NULL, 'Laptops', 'laptops', 'Ultrabooks, gaming, and creator laptops.', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800', 2, 'Laptops | VoltMart', 'Find laptops for work, play, and creation.'),
    (3, NULL, 'Audio', 'audio', 'Headphones, earbuds, and speakers.', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800', 3, 'Audio | VoltMart', 'Premium headphones and speakers.'),
    (4, NULL, 'Wearables', 'wearables', 'Smartwatches and fitness trackers.', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800', 4, 'Wearables | VoltMart', 'Smartwatches and fitness wearables.'),
    (5, NULL, 'TVs & Displays', 'tvs-displays', '4K TVs, monitors, and projectors.', 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800', 5, 'TVs & Displays | VoltMart', '4K TVs and professional monitors.'),
    (6, NULL, 'Accessories', 'accessories', 'Chargers, cases, cables, and more.', 'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=800', 6, 'Accessories | VoltMart', 'Chargers, cases, and cables.');

INSERT INTO categories (id, parent_id, name, slug, description, sort_order) VALUES
    (7, 1, 'Flagship Phones', 'flagship-phones', 'Premium flagship smartphones.', 1),
    (8, 1, 'Budget Phones', 'budget-phones', 'Value smartphones under $400.', 2),
    (9, 2, 'Ultrabooks', 'ultrabooks', 'Thin and light productivity laptops.', 1),
    (10, 2, 'Gaming Laptops', 'gaming-laptops', 'High-refresh gaming notebooks.', 2),
    (11, 3, 'Headphones', 'headphones', 'Over-ear and on-ear headphones.', 1),
    (12, 3, 'Earbuds', 'earbuds', 'True wireless earbuds.', 2);

INSERT INTO brands (name, slug, logo_url, description) VALUES
    ('Nexora', 'nexora', '', 'Premium smartphones and wearables.'),
    ('Aether', 'aether', '', 'Creator laptops and displays.'),
    ('PulseAudio', 'pulseaudio', '', 'Studio-grade audio gear.'),
    ('Lumen', 'lumen', '', 'TVs and visual technology.'),
    ('Orbit', 'orbit', '', 'Everyday accessories and power.');

INSERT INTO filter_definitions (category_id, name, slug, filter_type, unit, sort_order) VALUES
    (1, 'Screen Size', 'screen-size', 'RANGE', 'in', 1),
    (1, 'Storage', 'storage', 'MULTI', 'GB', 2),
    (1, '5G', '5g', 'BOOLEAN', NULL, 3),
    (2, 'RAM', 'ram', 'MULTI', 'GB', 1),
    (2, 'Processor', 'processor', 'MULTI', NULL, 2),
    (2, 'Screen Size', 'laptop-screen', 'RANGE', 'in', 3),
    (3, 'Driver Size', 'driver-size', 'RANGE', 'mm', 1),
    (3, 'Wireless', 'wireless', 'BOOLEAN', NULL, 2),
    (4, 'GPS', 'gps', 'BOOLEAN', NULL, 1),
    (5, 'Resolution', 'resolution', 'MULTI', NULL, 1),
    (5, 'Size', 'tv-size', 'RANGE', 'in', 2);

INSERT INTO products (id, sku, name, slug, short_description, description, category_id, brand_id, base_price, compare_at_price, discount_percent, warranty_months, weight_grams, featured, seo_title, seo_description) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'NX-P1-128', 'Nexora Pulse 1', 'nexora-pulse-1',
     'Flagship 6.7" OLED with 120Hz and triple camera.',
     'The Nexora Pulse 1 pairs a 6.7-inch LTPO OLED display with a custom N8 chip, 50MP main camera, and all-day silicon-carbon battery. IP68 rated with 2 years of OS updates.',
     1, 1, 899.00, 999.00, 10.00, 24, 189, TRUE, 'Nexora Pulse 1 | VoltMart', 'Buy the Nexora Pulse 1 flagship smartphone.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NX-LITE-64', 'Nexora Lite S', 'nexora-lite-s',
     'Compact 6.1" AMOLED with 90Hz and 5000mAh battery.',
     'Nexora Lite S is built for everyday speed: 90Hz AMOLED, dual 48MP cameras, and a 5000mAh battery that lasts two days.',
     1, 1, 329.00, 379.00, 13.00, 12, 172, TRUE, 'Nexora Lite S | VoltMart', 'Affordable Nexora Lite S smartphone.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'AE-AIR-14', 'Aether Air 14', 'aether-air-14',
     '1.1kg ultrabook with 14" 2.8K OLED and 18h battery.',
     'Aether Air 14 is a magnesium ultrabook with a 2.8K OLED panel, Intel Core Ultra 7, 32GB LPDDR5X, and Wi-Fi 7. Fanless chassis stays silent in meetings.',
     2, 2, 1499.00, 1699.00, 11.80, 24, 1100, TRUE, 'Aether Air 14 | VoltMart', 'Aether Air 14 OLED ultrabook.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'AE-GX-16', 'Aether GX 16', 'aether-gx-16',
     '16" 240Hz gaming laptop with RTX 4070.',
     'Aether GX 16 delivers 240Hz QHD, RTX 4070, and a vapor chamber cooler. RGB keyboard, 90Wh battery, and mux switch included.',
     2, 2, 1899.00, 2099.00, 9.50, 24, 2300, TRUE, 'Aether GX 16 | VoltMart', 'Aether GX 16 gaming laptop.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'PA-NOVA-X', 'PulseAudio Nova X', 'pulseaudio-nova-x',
     'Wireless over-ear with ANC and 40h battery.',
     'Nova X uses 40mm beryllium drivers, hybrid ANC, and LDAC. Fold-flat hinges and vegan leather earcups for travel.',
     3, 3, 249.00, 299.00, 16.70, 24, 265, TRUE, 'PulseAudio Nova X | VoltMart', 'PulseAudio Nova X ANC headphones.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'PA-TWINS', 'PulseAudio Twins Pro', 'pulseaudio-twins-pro',
     'True wireless earbuds with spatial audio.',
     'Twins Pro earbuds feature dual drivers, adaptive ANC, IP55, and wireless charging. 8h buds + 24h case.',
     3, 3, 179.00, 199.00, 10.00, 12, 52, FALSE, 'PulseAudio Twins Pro | VoltMart', 'PulseAudio Twins Pro earbuds.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 'NX-WATCH', 'Nexora Watch S3', 'nexora-watch-s3',
     'AMOLED smartwatch with ECG and 7-day battery.',
     'Watch S3 tracks ECG, SpO2, sleep stages, and GPS workouts. Sapphire crystal and titanium bezel.',
     4, 1, 329.00, 379.00, 13.00, 12, 38, TRUE, 'Nexora Watch S3 | VoltMart', 'Nexora Watch S3 smartwatch.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'LM-OLED-65', 'Lumen Cinema 65 OLED', 'lumen-cinema-65',
     '65" 4K 144Hz OLED TV with Dolby Vision.',
     'Cinema 65 OLED offers 144Hz VRR, Dolby Atmos, four HDMI 2.1 ports, and a calibrated Filmmaker Mode.',
     5, 4, 1799.00, 2199.00, 18.20, 24, 18500, TRUE, 'Lumen Cinema 65 OLED | VoltMart', '65-inch Lumen OLED TV.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', 'OR-GAN-65', 'Orbit GaN 65W Charger', 'orbit-gan-65w',
     'Compact 65W GaN charger with dual USB-C.',
     'Fold-flat 65W GaN brick charges a laptop and phone together. PPS and PD 3.1 supported.',
     6, 5, 39.00, 49.00, 20.00, 24, 98, FALSE, 'Orbit GaN 65W | VoltMart', 'Orbit 65W dual USB-C GaN charger.'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 'OR-CASE-P1', 'Orbit Armor Case Pulse 1', 'orbit-armor-pulse-1',
     'Drop-tested MagSafe case for Pulse 1.',
     'MIL-STD-810G drop protection with recycled polymer and MagSafe ring.',
     6, 5, 29.00, 39.00, 25.60, 12, 41, FALSE, 'Orbit Armor Case | VoltMart', 'Protective MagSafe case for Pulse 1.');

INSERT INTO product_images (product_id, url, alt_text, sort_order) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=1200', 'Nexora Pulse 1 front', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=1200', 'Nexora Pulse 1 camera', 1),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=1200', 'Nexora Lite S', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=1200', 'Aether Air 14', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=1200', 'Aether Air 14 keyboard', 1),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=1200', 'Aether GX 16', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1200', 'PulseAudio Nova X', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=1200', 'PulseAudio Twins Pro', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1200', 'Nexora Watch S3', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=1200', 'Lumen Cinema 65', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=1200', 'Orbit GaN charger', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=1200', 'Orbit Armor Case', 0);

INSERT INTO product_variants (id, product_id, sku, name, color, storage, price, compare_at_price, stock_qty) VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'NX-P1-128-BLK', '128GB Graphite', 'Graphite', '128GB', 899.00, 999.00, 42),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'NX-P1-256-SLV', '256GB Silver', 'Silver', '256GB', 999.00, 1099.00, 28),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NX-LITE-64-BLU', '64GB Ocean', 'Ocean', '64GB', 329.00, 379.00, 80),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'AE-AIR-14-32', '32GB / 1TB Silver', 'Silver', '1TB', 1499.00, 1699.00, 15),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb5', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'AE-GX-16-BLK', '32GB / 1TB Black', 'Black', '1TB', 1899.00, 2099.00, 9),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb6', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'PA-NOVA-BLK', 'Midnight', 'Midnight', NULL, 249.00, 299.00, 54),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb7', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'PA-TWINS-WHT', 'Pearl', 'Pearl', NULL, 179.00, 199.00, 70),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb8', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7', 'NX-WATCH-TI', 'Titanium 45mm', 'Titanium', NULL, 329.00, 379.00, 33),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb9', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'LM-OLED-65-BK', '65" Black', 'Black', NULL, 1799.00, 2199.00, 7),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb10', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9', 'OR-GAN-65-WH', 'White', 'White', NULL, 39.00, 49.00, 200),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb11', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10', 'OR-CASE-P1-CLR', 'Clear', 'Clear', NULL, 29.00, 39.00, 120);

INSERT INTO product_specs (product_id, group_name, spec_key, spec_value, sort_order) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Display', 'Size', '6.7 in LTPO OLED', 1),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Display', 'Refresh', '1-120 Hz', 2),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Performance', 'Chip', 'Nexora N8', 3),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Camera', 'Main', '50 MP f/1.6 OIS', 4),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Battery', 'Capacity', '5000 mAh', 5),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'Display', 'Panel', '14 in 2.8K OLED 120Hz', 1),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'Performance', 'CPU', 'Intel Core Ultra 7', 2),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'Memory', 'RAM', '32 GB LPDDR5X', 3),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'Audio', 'Drivers', '40 mm beryllium', 1),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'Battery', 'Playback', '40 hours', 2),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'Display', 'Panel', '65 in 4K OLED 144Hz', 1),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8', 'Audio', 'Sound', 'Dolby Atmos 2.2.2', 2);

INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', id, '6.7' FROM filter_definitions WHERE slug = 'screen-size';
INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', id, '128' FROM filter_definitions WHERE slug = 'storage';
INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', id, 'true' FROM filter_definitions WHERE slug = '5g';
INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', id, '32' FROM filter_definitions WHERE slug = 'ram';
INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', id, 'Intel Core Ultra 7' FROM filter_definitions WHERE slug = 'processor';

INSERT INTO reviews (product_id, user_id, rating, title, body, verified) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '55555555-5555-5555-5555-555555555555', 5, 'Best display I have used',
     'The 120Hz OLED is buttery and the cameras hold up at night. Battery easily lasts a day of maps and photos.', TRUE);

INSERT INTO banners (title, subtitle, image_url, cta_label, cta_url, placement, sort_order, active) VALUES
    ('Summer Circuit Sale', 'Up to 20% off flagship phones and OLED TVs.',
     'https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=1600', 'Shop deals', '/catalog?featured=true', 'HOME_HERO', 1, TRUE),
    ('Creator Laptops', 'Aether Air 14 — silent, light, 18-hour battery.',
     'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=1600', 'Explore laptops', '/catalog?category=laptops', 'HOME_HERO', 2, TRUE),
    ('Free shipping over ₹999', 'Standard delivery on most electronics.',
     'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=1200', 'Learn more', '/pages/shipping', 'HOME_STRIP', 1, TRUE);

INSERT INTO cms_pages (slug, title, body, seo_title, seo_description, published) VALUES
    ('about', 'About VoltMart',
     'VoltMart is an electronics retailer focused on transparent pricing, verified inventory, and two-year warranties on flagship devices. We ship across India with transparent delivery pricing.',
     'About VoltMart', 'Learn about VoltMart electronics retail.', TRUE),
    ('shipping', 'Shipping Policy',
     'Orders over $99 ship free via two-day ground. Express overnight is available at checkout. Tracking is emailed when the carrier scans the package.',
     'Shipping | VoltMart', 'VoltMart shipping policy and free shipping threshold.', TRUE),
    ('returns', 'Returns & Refunds',
     'Most items may be returned within 30 days in original packaging. Opened software and personalized accessories are final sale. Refunds post to the original payment method within 5 business days after inspection.',
     'Returns | VoltMart', '30-day returns and refund policy.', TRUE),
    ('warranty', 'Warranty',
     'Consumer electronics include a manufacturer warranty of 12 or 24 months as listed on the product page. VoltMart can facilitate claims and offer extended coverage at checkout.',
     'Warranty | VoltMart', 'Product warranty information.', TRUE);

INSERT INTO coupons (code, description, discount_type, amount, min_order, max_uses, starts_at, ends_at, active) VALUES
    ('WELCOME10', '10% off first order', 'PERCENT', 10.00, 50.00, 1000, NOW() - INTERVAL '1 day', NOW() + INTERVAL '365 days', TRUE),
    ('SHIPFREE', '$15 off shipping-eligible carts', 'FIXED', 15.00, 40.00, 500, NOW() - INTERVAL '1 day', NOW() + INTERVAL '90 days', TRUE);

INSERT INTO promotions (name, description, promo_type, promo_value, product_id, active, starts_at, ends_at) VALUES
    ('Pulse 1 launch', 'Featured homepage tile', 'FEATURED', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', TRUE, NOW() - INTERVAL '1 day', NOW() + INTERVAL '30 days');

SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));
SELECT setval('filter_definitions_id_seq', (SELECT MAX(id) FROM filter_definitions));
SELECT setval('product_images_id_seq', (SELECT MAX(id) FROM product_images));
SELECT setval('product_specs_id_seq', (SELECT MAX(id) FROM product_specs));
SELECT setval('banners_id_seq', (SELECT MAX(id) FROM banners));
SELECT setval('cms_pages_id_seq', (SELECT MAX(id) FROM cms_pages));
SELECT setval('coupons_id_seq', (SELECT MAX(id) FROM coupons));
SELECT setval('promotions_id_seq', (SELECT MAX(id) FROM promotions));
