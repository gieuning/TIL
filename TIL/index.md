# 인덱스 (Index)

## 네이밍 규칙

| 종류 | 형식 | 예시 |
|---|---|---|
| 일반 인덱스 | `idx_테이블명_컬럼명` | `idx_user_email`, `idx_order_created_at` |
| 유니크 인덱스 | `uk_테이블명_컬럼명` | `uk_product_external_id` |

---

## 인덱스를 두는 기준

- `WHERE`에 자주 쓰는 컬럼
- `JOIN`에 자주 쓰는 컬럼
- `ORDER BY`에 자주 쓰는 컬럼

---

## 인덱스 종류

### 단일 컬럼 인덱스
```sql
CREATE INDEX idx_product_name ON product(name);
```

### 복합 인덱스
```sql
CREATE INDEX idx_product_category_price ON product(category_id, price);
```

### 유니크 인덱스
```sql
CREATE UNIQUE INDEX uk_product_external_id ON product(external_id);
```

---

## 복합 인덱스 설계 예시

### 상황
- 조회 조건: `category_id`, `is_orderable`
- 정렬 조건: `price`, `created_at`

```sql
-- 조회 조건 + 정렬 조건을 모두 포함하는 복합 인덱스
CREATE INDEX idx_product_category_is_orderable_price_created_at
ON product(category_id, is_orderable, price, created_at);
```

> 조건 컬럼을 앞에, 정렬 컬럼을 뒤에 배치하는 게 기본 원칙

### 카테고리별 최신순 정렬이 많은 경우
```sql
-- SELECT * FROM product WHERE category_id = 1 ORDER BY created_at DESC;
CREATE INDEX idx_product_category_created_at ON product(category_id, created_at);
```