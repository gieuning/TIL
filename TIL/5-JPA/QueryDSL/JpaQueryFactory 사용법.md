## 기본 조회

```java
@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<Product> findProductV1() {
      return queryFactory.selectFrom(product).fetch();
  }
}
```

---

## 결과 조회 메서드

| 메서드 | 설명 |
|--------|------|
| `fetch()` | 리스트 반환. 결과 없으면 빈 리스트 |
| `fetchOne()` | 단건 반환. 결과 없으면 null, 2개 이상이면 예외 |
| `fetchFirst()` | 첫 번째 결과만 반환 (`limit(1).fetchOne()` 과 동일) |
| `fetchCount()` | COUNT 쿼리 실행, 개수 반환 |

```java
List<Product> list = queryFactory.selectFrom(product).fetch();

Product one = queryFactory.selectFrom(product)
    .where(product.id.eq(1L))
    .fetchOne();

Product first = queryFactory.selectFrom(product)
    .orderBy(product.createdAt.desc())
    .fetchFirst();

long count = queryFactory.selectFrom(product).fetchCount();
```

---

## 동적 쿼리
검색 조건이 있을 때만 WHERE절에 추가하는 동적 쿼리를 쉽게 작성할 수 있다.
where() 안의 조건이 null이면 자동으로 무시된다.

V1 - 인라인 방식

```java
  public List<Product> findProductsV1(String name, Double minPrice, Double maxPrice) {
  return queryFactory
      .selectFrom(product)
      .join(category).on(product.category.eq(category))
      .where(
          name != null ? product.name.contains(name) : null,  // null이면 무시 (AND 조건)
          minPrice != null ? product.price.goe(minPrice) : null, // goe: >=
          maxPrice != null ? product.price.loe(maxPrice) : null  // loe: <=
      )
      .fetch();
}

```

V2 - 조건을 메서드로 분리 (권장)

조건이 많아질수록 메서드로 분리하면 재사용성과 가독성이 올라간다.

```java
 public List<Product> findProductsV2(String name, Double minPrice, Double maxPrice) {
      return queryFactory
          .selectFrom(product)
          .where(
              nameContains(name),
              priceGoe(minPrice),
              priceLoe(maxPrice)
          )
          .fetch();
  }

  private BooleanExpression nameContains(String name) {
      return name != null ? product.name.contains(name) : null;
  }

  private BooleanExpression priceGoe(Double minPrice) {
      return minPrice != null ? product.price.goe(minPrice) : null;
  }

  private BooleanExpression priceLoe(Double maxPrice) {
      return maxPrice != null ? product.price.loe(maxPrice) : null;
  }

```

---

## 정렬

```java
// 단일 정렬
queryFactory.selectFrom(product)
    .orderBy(product.price.desc())
    .fetch();

// 복수 정렬 (가격 내림차순 → 이름 오름차순)
queryFactory.selectFrom(product)
    .orderBy(product.price.desc(), product.name.asc())
    .fetch();
```

---

## 페이징

```java
List<Product> result = queryFactory
    .selectFrom(product)
    .orderBy(product.id.asc())
    .offset(0)   // 시작 위치 (0부터)
    .limit(10)   // 최대 개수
    .fetch();

long total = queryFactory
    .selectFrom(product)
    .fetchCount();
```

---

## 프로젝션 (특정 컬럼 선택)

특정 필드만 조회하거나, DTO로 바로 받을 수 있다.

```java
// 단일 컬럼
List<String> names = queryFactory
    .select(product.name)
    .from(product)
    .fetch();

// 여러 컬럼 → Tuple
List<Tuple> tuples = queryFactory
    .select(product.name, product.price)
    .from(product)
    .fetch();

String name = tuples.get(0).get(product.name);
Double price = tuples.get(0).get(product.price);

// DTO 직접 매핑 (Projections.constructor)
List<ProductDto> dtos = queryFactory
    .select(Projections.constructor(ProductDto.class,
        product.name,
        product.price))
    .from(product)
    .fetch();
```

> DTO 매핑은 `@QueryProjection`을 사용하는 방법도 있다.

---

## 집계 함수

```java
// 전체 개수
long count = queryFactory.select(product.count()).from(product).fetchOne();

// 합계 / 평균 / 최대 / 최소
queryFactory
    .select(
        product.count(),
        product.price.sum(),
        product.price.avg(),
        product.price.max(),
        product.price.min()
    )
    .from(product)
    .fetchOne();
```

groupBy / having

```java
List<Tuple> result = queryFactory
    .select(product.category, product.price.avg())
    .from(product)
    .groupBy(product.category)
    .having(product.price.avg().gt(10000))  // 평균 가격 10000 초과
    .fetch();
```

---

## 조인

```java
// inner join
queryFactory.selectFrom(product)
    .join(product.category, category)
    .fetch();

// left join
queryFactory.selectFrom(product)
    .leftJoin(product.category, category)
    .fetch();

// fetch join (N+1 해결)
queryFactory.selectFrom(product)
    .join(product.category, category).fetchJoin()
    .fetch();

// on절 (조인 조건 추가)
queryFactory.selectFrom(product)
    .leftJoin(category).on(product.category.eq(category))
    .fetch();
```

---

## 수정 / 삭제

벌크 연산은 영속성 컨텍스트를 거치지 않고 DB에 직접 반영되므로,
실행 후 `em.flush()` + `em.clear()` 또는 `entityManager.clear()`를 호출해야 한다.

```java
// 수정
long updatedCount = queryFactory
    .update(product)
    .set(product.price, product.price.multiply(1.1))  // 가격 10% 인상
    .where(product.category.name.eq("전자제품"))
    .execute();

// 삭제
long deletedCount = queryFactory
    .delete(product)
    .where(product.price.lt(1000))
    .execute();
```