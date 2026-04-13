# テストケース

## 1. 単体テスト

### TC-UNIT-001 在庫不足時は注文登録に失敗する
- 対象: `OrderServiceImpl`
- 入力: productId=1, quantity=10
- 前提: currentStock=3
- 期待結果:
  - `InsufficientStockException` が発生する
  - `orders` への登録は行われない

### TC-UNIT-002 正常な注文は合計金額を計算して登録される
- 対象: `OrderServiceImpl`
- 入力: 商品1を2個
- 前提: 単価780円、在庫10
- 期待結果:
  - `totalAmount = 1560.00`
  - `order_items` が1件登録される
  - `inventory_movements` に `OUT -2` が登録される

---

## 2. 結合テスト

### TC-INT-001 認証ありで日別売上APIを呼ぶと200が返る
- 対象: `GET /api/reports/sales/daily`
- 認証: `staff / mountain123`
- 入力: `from=2026-04-01`, `to=2026-04-30`
- 期待結果:
  - HTTP 200
  - `$.data` が配列
  - 先頭日付が `2026-04-02`

### TC-INT-002 認証なしで日別売上APIを呼ぶと401が返る
- 対象: `GET /api/reports/sales/daily`
- 認証: なし
- 期待結果:
  - HTTP 401

---

## 3. 手動テスト（正常系）

### TC-MAN-001 商品登録
- 認証: admin
- API: `POST /api/admin/products`
- 期待結果:
  - HTTP 201
  - 商品が登録される

### TC-MAN-002 在庫入庫
- 認証: admin
- API: `POST /api/admin/inventory/movements`
- 期待結果:
  - HTTP 201
  - `quantityDelta` が正で登録される

### TC-MAN-003 注文登録
- 認証: staff
- API: `POST /api/orders`
- 期待結果:
  - HTTP 201
  - `orders`, `order_items`, `inventory_movements` が更新される

### TC-MAN-004 売れ筋商品集計
- 認証: staff
- API: `GET /api/reports/sales/top-products`
- 期待結果:
  - HTTP 200
  - 数量順で並ぶ

---

## 4. 手動テスト（異常系）

### TC-MAN-005 重複商品コードで商品登録
- 認証: admin
- API: `POST /api/admin/products`
- 条件: 既存コード `FOOD-001`
- 期待結果:
  - HTTP 400
  - 「同じ商品コードが既に存在します。」

### TC-MAN-006 存在しない商品で在庫更新
- 認証: admin
- API: `POST /api/admin/inventory/movements`
- 条件: `productId=999`
- 期待結果:
  - HTTP 404

### TC-MAN-007 在庫不足の注文
- 認証: staff
- API: `POST /api/orders`
- 条件: 在庫を超える数量を注文
- 期待結果:
  - HTTP 400
  - 在庫不足メッセージ

### TC-MAN-008 staffで管理者APIを実行
- 認証: staff
- API: `POST /api/admin/products`
- 期待結果:
  - HTTP 403
