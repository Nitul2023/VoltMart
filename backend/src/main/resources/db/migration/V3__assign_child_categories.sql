UPDATE products SET category_id = 7 WHERE sku = 'NX-P1-128';
UPDATE products SET category_id = 8 WHERE sku = 'NX-LITE-64';
UPDATE products SET category_id = 9 WHERE sku = 'AE-AIR-14';
UPDATE products SET category_id = 10 WHERE sku = 'AE-GX-16';
UPDATE products SET category_id = 11 WHERE sku = 'PA-NOVA-X';
UPDATE products SET category_id = 12 WHERE sku = 'PA-TWINS';

INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', id, 'true' FROM filter_definitions WHERE slug = 'wireless';
INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', id, 'true' FROM filter_definitions WHERE slug = 'wireless';
INSERT INTO product_filter_values (product_id, filter_id, value_text)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', id, '40' FROM filter_definitions WHERE slug = 'driver-size';
