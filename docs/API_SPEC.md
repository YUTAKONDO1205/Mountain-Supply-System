# API仕様書

## 認証
すべての `/api/**` は Basic認証です。  
ただし `GET /api/health` と `/h2-console/**` は認証不要です。

---

## 1. ヘルスチェック
### GET `/api/health`

**Response 200**
```json
{
  "status": "ok"
}
```

---

## 2. 商品登録
### POST `/api/admin/products`
管理者のみ。

**Request**
```json
{
  "code": "FOOD-010",
  "name": "山頂ラーメン",
  "category": "食品",
  "unitPrice": 620.00,
  "reorderPoint": 15
}
```

**Response 201**
```json
{
  "message": "商品を登録しました。",
  "data": {
    "id": 6,
    "code": "FOOD-010",
    "name": "山頂ラーメン",
    "category": "食品",
    "unitPrice": 620.00,
    "reorderPoint": 15
  }
}
```

---

## 3. 商品一覧
### GET `/api/products`

**Response 200**
```json
{
  "message": "商品一覧を取得しました。",
  "data": [
    {
      "id": 1,
      "code": "FOOD-001",
      "name": "フリーズドライカレー",
      "category": "食品",
      "unitPrice": 780.00,
      "reorderPoint": 20
    }
  ]
}
```

---

## 4. 在庫更新
### POST `/api/admin/inventory/movements`
管理者のみ。

**Request**
```json
{
  "productId": 1,
  "movementType": "IN",
  "quantity": 30,
  "note": "追加納品"
}
```

**Response 201**
```json
{
  "message": "在庫移動を登録しました。",
  "data": {
    "id": 100,
    "productId": 1,
    "movementType": "IN",
    "quantityDelta": 30,
    "referenceType": "ADMIN",
    "referenceId": null,
    "note": "追加納品",
    "createdAt": "2026-04-13T15:00:00"
  }
}
```

---

## 5. 現在庫一覧
### GET `/api/inventory/stocks`

**Response 200**
```json
{
  "message": "現在庫一覧を取得しました。",
  "data": [
    {
      "productId": 1,
      "code": "FOOD-001",
      "name": "フリーズドライカレー",
      "category": "食品",
      "reorderPoint": 20,
      "currentStock": 87
    }
  ]
}
```

---

## 6. 低在庫一覧
### GET `/api/inventory/low-stocks`

**Response 200**
```json
{
  "message": "低在庫一覧を取得しました。",
  "data": [
    {
      "productId": 4,
      "code": "SAFE-001",
      "name": "救急セット",
      "currentStock": 8,
      "reorderPoint": 8,
      "recent30DaySales": 2
    }
  ]
}
```

---

## 7. 注文登録
### POST `/api/orders`

**Request**
```json
{
  "customerName": "春山合宿チーム",
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

**Response 201**
```json
{
  "message": "注文を登録しました。",
  "data": {
    "orderId": 10,
    "customerName": "春山合宿チーム",
    "orderStatus": "CONFIRMED",
    "totalAmount": 2210.00,
    "orderedAt": "2026-04-13T15:10:00",
    "createdBy": "staff",
    "items": [
      {
        "productId": 1,
        "productCode": "FOOD-001",
        "productName": "フリーズドライカレー",
        "quantity": 2,
        "unitPrice": 780.00,
        "lineAmount": 1560.00
      },
      {
        "productId": 3,
        "productCode": "GEAR-001",
        "productName": "ガスカートリッジ",
        "quantity": 1,
        "unitPrice": 650.00,
        "lineAmount": 650.00
      }
    ]
  }
}
```

---

## 8. 注文詳細
### GET `/api/orders/{orderId}`

---

## 9. 日別売上
### GET `/api/reports/sales/daily?from=2026-04-01&to=2026-04-30`

---

## 10. 月別売上
### GET `/api/reports/sales/monthly`

---

## 11. 売れ筋商品
### GET `/api/reports/sales/top-products?from=2026-04-01&to=2026-04-30&limit=5`

---

## 12. カテゴリ別売上
### GET `/api/reports/sales/by-category?from=2026-04-01&to=2026-04-30`

---

## 13. ダッシュボード
### GET `/api/reports/dashboard`

---

## エラー例
### Response 400
```json
{
  "timestamp": "2026-04-13T15:30:00",
  "status": 400,
  "message": "在庫不足です。 product=ガスカートリッジ, currentStock=2, requested=5"
}
```

### Response 401
認証情報なし。

### Response 403
権限不足。

### Response 404
存在しない商品・注文を参照。
