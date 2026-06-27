# 山小屋補給品 在庫・受注・売上管理システム

開発者: 近藤悠太 (Kondo Yuta)

Java / SQL / Web / Git / テストを盛り込んだ、**業務アプリ風ミニシステム**です。  
テーマは「山小屋補給品管理」。ユーザーの登山文脈を少し入れつつ、やっていることは業務システムです。

## このシステムについて

### Java基礎
- クラス: `Product`, `OrderHeader`, `InventoryMovement`
- 継承: `BusinessException` → `NotFoundException`, `InsufficientStockException`
- インターフェース: `ProductService`, `InventoryService`, `OrderService`, `ReportService`
- 例外: 業務例外、在庫不足例外、バリデーション例外
- コレクション / ラムダ: `stream()`, `map()`, `collect()`, `forEach()`

### SQL基礎
- `SELECT`: 商品一覧、在庫一覧、注文詳細
- `JOIN`: 売上集計、注文詳細、カテゴリ別売上
- `GROUP BY`: 日別売上、月別売上、売れ筋商品、在庫集計
- サブクエリ: 低在庫商品一覧
- インデックス: `schema.sql` に定義
- 正規化: `products`, `orders`, `order_items`, `inventory_movements`, `app_users` に分割

### Web基礎
- HTTPメソッド: `GET`, `POST`
- REST API: `/api/...`
- JSON入出力
- 認証: Basic認証
- ステータスコード: `200`, `201`, `400`, `401`, `403`, `404`

### Git/GitHub運用
- featureブランチで作業
- PRでレビュー
- issueを切って機能単位で実装

### テスト基礎
- 単体テスト: `OrderServiceImplTest`, `ProductServiceImplTest`, `InventoryServiceImplTest`
- 結合テスト: `ReportControllerIntegrationTest`, `MssApiIntegrationTest`
- 正常系 / 異常系の確認

> 補足1: 初期データ `data.sql` は IDENTITY 列に明示的IDを投入しているため、
> シーケンスを `ALTER TABLE ... ALTER COLUMN id RESTART WITH n` で次の採番位置へ進めています。
> これを行わないと、起動後に最初の `POST`（注文・商品登録）で id=1 が再採番され主キー衝突になります。
>
> 補足2: 商品登録の `SimpleJdbcInsert` は `usingColumns(...)` で投入列を明示しています。
> 指定しないとテーブル全列が対象になり、`created_at` / `updated_at` / `is_active` に
> `NULL` がバインドされて `NOT NULL` 制約違反になります（DBのDEFAULTに任せるため列を絞る）。

---

## 業務機能
- 商品登録 / 商品更新 / 商品削除（論理削除 `is_active`）
- 在庫更新 / 在庫移動履歴 / 現在庫（一覧・単体）
- 注文登録 / 注文一覧（期間・ステータス絞り込み＋ページング）
- 注文ライフサイクル: `CONFIRMED` → `SHIPPED`（出荷） / `CANCELLED`（キャンセル・在庫戻し）
- 売上集計
- ログイン認証
- DB保存（H2インメモリDB）
- API仕様書
- テストケース

> 注文ステータスの遷移ルール:
> - 出荷（ship）は `CONFIRMED` のみ可 → `SHIPPED`
> - キャンセル（cancel）は `CONFIRMED` のみ可 → `CANCELLED`（明細分を `ORDER_CANCEL` で入庫）
> - 出荷済み・キャンセル済みからの再操作は 400

---

## 技術スタック
- Java 21
- Spring Boot 3.5.11
- Spring Web
- Spring Security
- Spring JDBC
- H2 Database
- JUnit 5

---

## 起動方法
このzipには `pom.xml` を含めています。ローカルで Maven が使える環境で実行してください。

```bash
mvn spring-boot:run
```

起動後:
- API: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

H2接続情報:
- JDBC URL: `jdbc:h2:mem:mountaindb`
- User: `sa`
- Password: 空欄

---

## ログイン情報
- 管理者: `admin / mountain123`
- スタッフ: `staff / mountain123`

※ 学習用のため `{noop}` パスワードを使用しています。実務では必ずハッシュ化してください。

---

## ディレクトリ構成
```text
src/main/java/com/kondo/mss
├── auth
├── common
├── inventory
├── order
├── product
└── report
```

---

## SQLを多めに入れたポイント
1. 在庫一覧  
   `products` と `inventory_movements` を `LEFT JOIN` し、`SUM(quantity_delta)` で現在庫を計算。

2. 低在庫一覧  
   在庫集計のサブクエリと、30日販売数の集計サブクエリを組み合わせています。

3. 日別 / 月別売上  
   `orders` と `order_items` を `JOIN` し、`GROUP BY` で集計。

4. 売れ筋商品  
   商品軸で `GROUP BY` してランキング形式で取得。

5. カテゴリ別売上  
   `products.category` まで `JOIN` して集計。

---

## 正規化の考え方
- `products`: 商品マスタ
- `inventory_movements`: 入出庫履歴
- `orders`: 注文ヘッダ
- `order_items`: 注文明細
- `app_users`: ログインユーザー

これにより、
- 商品情報の重複を減らせる
- 注文と明細を分離できる
- 在庫を履歴ベースで追える
- 売上集計SQLを書きやすい

---

## API仕様書 / テストケース
- [API仕様書](docs/API_SPEC.md)
- [テストケース](docs/TEST_CASES.md)
- [SQL学習メモ](docs/SQL_STUDY_GUIDE.md)
