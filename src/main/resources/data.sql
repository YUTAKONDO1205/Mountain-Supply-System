INSERT INTO app_users (id, username, password, role, full_name) VALUES
(1, 'admin', '{noop}mountain123', 'ADMIN', '山小屋管理者'),
(2, 'staff', '{noop}mountain123', 'STAFF', '補給担当スタッフ');

INSERT INTO products (id, code, name, category, unit_price, reorder_point) VALUES
(1, 'FOOD-001', 'フリーズドライカレー', '食品', 780.00, 20),
(2, 'FOOD-002', '行動食チョコバー', '食品', 250.00, 50),
(3, 'GEAR-001', 'ガスカートリッジ', '装備', 650.00, 15),
(4, 'SAFE-001', '救急セット', '安全', 1800.00, 8),
(5, 'DRINK-001', 'ミネラルウォーター', '飲料', 180.00, 40);

INSERT INTO inventory_movements (product_id, movement_type, quantity_delta, reference_type, reference_id, note) VALUES
(1, 'IN', 100, 'INITIAL', NULL, '初期在庫'),
(2, 'IN', 200, 'INITIAL', NULL, '初期在庫'),
(3, 'IN', 80, 'INITIAL', NULL, '初期在庫'),
(4, 'IN', 10, 'INITIAL', NULL, '初期在庫'),
(5, 'IN', 150, 'INITIAL', NULL, '初期在庫'),
(1, 'OUT', -10, 'LOSS', NULL, '破損ロス'),
(3, 'OUT', -5, 'USE', NULL, '緊急利用');

INSERT INTO orders (id, customer_name, order_status, total_amount, ordered_at, created_by) VALUES
(1, '北アルプス縦走隊', 'CONFIRMED', 3310.00, TIMESTAMP '2026-04-02 10:00:00', 'staff'),
(2, '春山トレーニング班', 'CONFIRMED', 3340.00, TIMESTAMP '2026-04-05 09:30:00', 'staff'),
(3, '山岳救助訓練', 'CONFIRMED', 5580.00, TIMESTAMP '2026-04-10 14:20:00', 'admin');

INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_amount) VALUES
(1, 1, 2, 780.00, 1560.00),
(1, 2, 3, 250.00, 750.00),
(1, 5, 5, 180.00, 900.00),
(1, 3, 1, 100.00, 100.00),
(2, 1, 1, 780.00, 780.00),
(2, 2, 4, 250.00, 1000.00),
(2, 3, 2, 650.00, 1300.00),
(2, 5, 2, 130.00, 260.00),
(3, 4, 2, 1800.00, 3600.00),
(3, 3, 2, 650.00, 1300.00),
(3, 5, 3, 180.00, 540.00),
(3, 2, 1, 140.00, 140.00);

INSERT INTO inventory_movements (product_id, movement_type, quantity_delta, reference_type, reference_id, note) VALUES
(1, 'OUT', -2, 'ORDER', 1, '受注出庫'),
(2, 'OUT', -3, 'ORDER', 1, '受注出庫'),
(5, 'OUT', -5, 'ORDER', 1, '受注出庫'),
(3, 'OUT', -1, 'ORDER', 1, '受注出庫'),
(1, 'OUT', -1, 'ORDER', 2, '受注出庫'),
(2, 'OUT', -4, 'ORDER', 2, '受注出庫'),
(3, 'OUT', -2, 'ORDER', 2, '受注出庫'),
(5, 'OUT', -2, 'ORDER', 2, '受注出庫'),
(4, 'OUT', -2, 'ORDER', 3, '受注出庫'),
(3, 'OUT', -2, 'ORDER', 3, '受注出庫'),
(5, 'OUT', -3, 'ORDER', 3, '受注出庫'),
(2, 'OUT', -1, 'ORDER', 3, '受注出庫');

-- IDENTITY列に明示的IDを投入したため、シーケンスの現在値を次の採番位置へ進める。
-- これを行わないと、実行時の最初のINSERTでid=1が再採番され主キー衝突（500）になる。
ALTER TABLE app_users ALTER COLUMN id RESTART WITH 3;
ALTER TABLE products ALTER COLUMN id RESTART WITH 6;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 4;
