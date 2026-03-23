# @QueryProjection

DTO 생성자에 붙이면 해당 DTO의 Q클래스도 자동으로 생성된다.
`Projections.constructor()`보다 **컴파일 타임에 타입 체크**가 가능해서 더 안전하다.

---

## 설정

DTO 생성자에 `@QueryProjection`을 붙인다.

```java
@Getter
public class ProductDto {

    private final String name;
    private final Double price;

    @QueryProjection
    public ProductDto(String name, Double price) {
        this.name = name;
        this.price = price;
    }
}
```

`./gradlew compileJava` 실행 시 `QProductDto`가 자동 생성된다.

---

## 사용법

```java
List<ProductDto> result = queryFactory
    .select(new QProductDto(
        product.name,
        product.price
    ))
    .from(product)
    .fetch();
```

`new QProductDto()`를 사용하면 IDE에서 파라미터 타입/순서를 잘못 넣었을 때 **컴파일 에러**로 잡아준다.

---

## Projections.constructor()와 비교

| | `@QueryProjection` | `Projections.constructor()` |
|---|---|---|
| 타입 체크 | 컴파일 타임 | 런타임 |
| 오타/순서 오류 | 컴파일 에러로 즉시 발견 | 런타임 에러 |
| QueryDSL 의존성 | DTO가 QueryDSL에 의존함 | DTO가 순수 객체 유지 |
| 권장 상황 | 안전성이 중요할 때 | DTO를 QueryDSL과 분리하고 싶을 때 |

```java
// Projections.constructor - 런타임에만 오류 발견
List<ProductDto> result = queryFactory
    .select(Projections.constructor(ProductDto.class,
        product.name,
        product.price))
    .from(product)
    .fetch();
```

---

## 주의사항

- DTO가 QueryDSL에 **의존하게 된다**는 단점이 있다. (레이어 분리 관점에서 고려 필요)
- Q클래스이므로 마찬가지로 `.gitignore`에 생성 경로를 추가해야 한다.
- 생성자가 여러 개면 각각에 `@QueryProjection`을 붙일 수 있다.