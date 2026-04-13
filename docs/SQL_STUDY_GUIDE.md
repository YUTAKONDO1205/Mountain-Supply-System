# SQLメモ

## 1. SELECT
### 商品一覧
```sql
SELECT id, code, name, category, unit_price, reorder_point
FROM products
ORDER BY id;
```

## 2. JOIN
### 注文詳細
```sql
SELECT oi.product_id,
       p.code AS product_code,
       p.name AS product_name,
       oi.quantity,
       oi.unit_price,
       oi.line_amount
FROM order_items oi
JOIN products p ON p.id = oi.product_id
WHERE oi.order_id = ?
ORDER BY oi.id;
```

## 3. GROUP BY
### 日別売上
```sql
SELECT CAST(o.ordered_at AS DATE) AS sale_date,
       COUNT(DISTINCT o.id) AS order_count,
       SUM(oi.quantity) AS total_quantity,
       SUM(oi.line_amount) AS total_sales
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
WHERE CAST(o.ordered_at AS DATE) BETWEEN ? AND ?
GROUP BY CAST(o.ordered_at AS DATE)
ORDER BY sale_date;
```

## 4. サブクエリ
### 低在庫一覧
```sql
SELECT s.product_id,
       s.code,
       s.name,
       s.current_stock,
       s.reorder_point,
       COALESCE(r.recent_30_day_sales, 0) AS recent_30_day_sales
FROM (
    SELECT p.id AS product_id,
           p.code,
           p.name,
           p.reorder_point,
           COALESCE(SUM(im.quantity_delta), 0) AS current_stock
    FROM products p
    LEFT JOIN inventory_movements im ON im.product_id = p.id
    GROUP BY p.id, p.code, p.name, p.reorder_point
) s
LEFT JOIN (
    SELECT oi.product_id,
           SUM(oi.quantity) AS recent_30_day_sales
    FROM order_items oi
    JOIN orders o ON o.id = oi.order_id
    WHERE o.ordered_at >= DATEADD('DAY', -30, CURRENT_TIMESTAMP)
    GROUP BY oi.product_id
) r ON r.product_id = s.product_id
WHERE s.current_stock <= s.reorder_point
ORDER BY s.current_stock ASC, s.name ASC;
```

## 5. インデックス
`schema.sql` では以下のようなインデックスを貼っています。

- `idx_products_code`
- `idx_inventory_movements_product_id`
- `idx_orders_ordered_at`
- `idx_order_items_order_product`

### なぜ必要か
- 商品コードの重複チェックを速くする
- 商品ごとの在庫集計を速くする
- 日付条件つき売上集計を速くする
- 注文と明細の参照を速くする

## 6. 正規化

### products
商品マスタ

### orders
注文ヘッダ

### order_items
注文の明細。1注文に複数商品を持てる。

### inventory_movements
在庫の増減履歴。現在庫はこの履歴の合計で出す。

### app_users
ログインユーザー
