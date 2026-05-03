# 단건 조회에서 쿼리 3번 → @EntityGraph로 1번으로 줄인 경험

---

## N+1 문제란? (배경 지식)

1번의 쿼리로 N개의 결과를 가져온 뒤, 각 결과의 연관관계를 조회하기 위해 N번의 추가 쿼리가 발생하는 문제.

예를 들어 상품 10개를 조회하면:
``` sql
SELECT * FROM products                       -- 1번
SELECT * FROM categories WHERE id = ?        -- 10번 (상품마다)
= 총 11번 (1 + N)
```

---

## 내가 겪은 문제 - 단건 조회에서 Lazy Loading 추가 쿼리

N+1과는 다르게, **단건 조회(findById)** 이후 연관관계 필드에 접근할 때마다 추가 쿼리가 발생한 케이스다.

`getProduct`에서 `findById`로 Product를 가져온 뒤,
`product.getOptions()`와 `product.getCategory()`에 접근하면서 쿼리가 **3번** 나갔다.

```
findById → products 조회         (1번)
getCategory() → categories 조회  (1번)  ← Lazy Loading
getOptions() → product_options 조회 (1번)  ← Lazy Loading
= 총 3번
```

### 왜 이런 일이 생기나?

JPA는 연관관계를 기본적으로 **Lazy Loading(지연 로딩)** 으로 처리한다.
즉, 실제로 해당 필드에 접근하는 시점에 SELECT 쿼리가 나간다.

> 연관관계를 항상 즉시 로딩하면 필요 없는 데이터까지 매번 JOIN해서 가져오기 때문에,
> "필요할 때만 가져온다"는 게 기본 전략이다.
> 필요한 경우에만 명시적으로 즉시 로딩을 적용하는 게 맞는 방향.

---

## 기존 코드

**서비스 로직**

```java
@Transactional(readOnly = true)
  public ProductDetailResponse getProduct(Long productId) {
    Product product = findByProduct(productId);

    CategoryResponse categoryResponse = CategoryResponse.builder()
        .id(product.getCategory().getId())
        .name(product.getCategory().getName())
        .build();

    List<OptionResponse> options = product.getOptions().stream()
        .map(o -> OptionResponse.builder()
            .id(o.getId())
            .name(o.getName())
            .additionalPrice(o.getAdditionalPrice())
            .stock(o.getStock())
            .build())
        .toList();

    return ProductDetailResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .stock(product.getStock())
        .category(categoryResponse)
        .options(options)
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
```

<details>
<summary>실행된 쿼리 (3번)</summary>

```sql
select
    p1_0.id,
    p1_0.category_id,
    p1_0.created_at,
    p1_0.description,
    p1_0.name,
    p1_0.price,
    p1_0.product_status,
    p1_0.stock,
    p1_0.updated_at
from
    products p1_0
where
    p1_0.id=?

select
    c1_0.id,
    c1_0.created_at,
    c1_0.description,
    c1_0.name,
    c1_0.parent_id,
    c1_0.updated_at
from
    categories c1_0
where
    c1_0.id=?

select
    o1_0.product_id,
    o1_0.id,
    o1_0.option_price,
    o1_0.created_at,
    o1_0.name,
    o1_0.stock,
    o1_0.updated_at
from
    product_options o1_0
where
    o1_0.product_id=?
```

</details>

---

## 해결법

1. **@EntityGraph** - 연관 엔티티를 어떻게 로딩할지 정의하는 데 쓰인다.
2. **FetchJoin** - JPQL에서 직접 JOIN FETCH로 명시하는 방법.

---

## 적용 - Repository 수정

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  boolean existsByCategory(Category category);

  @EntityGraph(attributePaths = {"category", "options"})
  Optional<Product> findWithDetailsById(Long id);

  // FetchJoin 방식 (대안)
  @Query("SELECT p FROM Product p " +
      "JOIN FETCH p.category " +
      "LEFT JOIN FETCH p.options " +
      "WHERE p.id = :id")
  Optional<Product> findWithDetailsByIdJpql(@Param("id") Long id);
}
```

## 적용 - Service 수정

```java
@Transactional(readOnly = true)
  public ProductDetailResponse getProduct(Long productId) {
    Product product = productRepository.findWithDetailsById(productId)
        .orElseThrow(() -> new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT));

    CategoryResponse categoryResponse = CategoryResponse.builder()
        .id(product.getCategory().getId())
        .name(product.getCategory().getName())
        .build();

    List<OptionResponse> options = product.getOptions().stream()
        .map(o -> OptionResponse.builder()
            .id(o.getId())
            .name(o.getName())
            .additionalPrice(o.getAdditionalPrice())
            .stock(o.getStock())
            .build())
        .toList();

    return ProductDetailResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .stock(product.getStock())
        .category(categoryResponse)
        .options(options)
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
```

<details>
<summary>실행된 쿼리 (1번으로 줄어든 결과)</summary>

```sql
select
        p1_0.id,
        c1_0.id,
        c1_0.created_at,
        c1_0.description,
        c1_0.name,
        c1_0.parent_id,
        c1_0.updated_at,
        p1_0.created_at,
        p1_0.description,
        p1_0.name,
        o1_0.product_id,
        o1_0.id,
        o1_0.option_price,
        o1_0.created_at,
        o1_0.name,
        o1_0.stock,
        o1_0.updated_at,
        p1_0.price,
        p1_0.product_status,
        p1_0.stock,
        p1_0.updated_at
    from
        products p1_0
    left join
        categories c1_0
            on c1_0.id=p1_0.category_id
    left join
        product_options o1_0
            on p1_0.id=o1_0.product_id
    where
        p1_0.id=?
```

</details>

---

## @EntityGraph 정리

JPA는 기본적으로 연관관계를 지연 로딩으로 처리하는데, 이 때문에 연관 필드에 접근할 때마다 추가 쿼리가 발생할 수 있다.
`@EntityGraph`는 이를 해결하기 위해 **특정 쿼리에서만 즉시 로딩(Eager Loading)** 을 적용할 수 있게 해준다.

### 사용 예시

```java
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderItem> items;
}
```

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // member와 items를 함께 JOIN FETCH로 가져옴
    @EntityGraph(attributePaths = {"member", "items"})
    List<Order> findAll();
}
```

### 주요 속성

- `attributePaths` - 즉시 로딩할 연관 필드명 지정
- `type` - `FETCH`(지정한 것만 Eager) 또는 `LOAD`(기존 설정 유지 + 지정한 것 Eager)

```java
@EntityGraph(attributePaths = {"category"}, type = EntityGraph.EntityGraphType.FETCH)
//           ↑ 즉시 로딩할 필드          ↑ 기본값 - 지정한 것만 Eager, 나머지는 Lazy 유지

@EntityGraph(attributePaths = {"category"}, type = EntityGraph.EntityGraphType.LOAD)
//                                          ↑ 지정한 것 Eager + 엔티티에 선언된 기존 설정 유지
```