# Q클래스란?

`./gradlew compileJava` 실행 시 JPA 엔티티를 분석해서 자동으로 생성되는 클래스다.

```
Product (엔티티) → QProduct (자동 생성)
```

예를 들어 Product 엔티티가 있으면 QProduct가 생성되고, 필드를 타입 안전하게 참조할 수 있다.

---

## 생성 원리

`@Entity`가 붙은 클래스를 **APT(Annotation Processing Tool)** 가 컴파일 시점에 분석해서 Q클래스를 자동 생성한다.

```
src/main/java/...Product.java  (원본 엔티티)
         ↓ compileJava
build/generated/...QProduct.java  (자동 생성)
```

---

## Q클래스 내부 구조

```java
// 자동 생성된 QProduct (예시)
public class QProduct extends EntityPathBase<Product> {

    public final StringPath name = createString("name");
    public final NumberPath<Double> price = createNumber("price", Double.class);
    public final EnumPath<Category> category = createEnum("category", Category.class);

    public static final QProduct product = new QProduct("product");
}
```

각 필드는 타입에 맞는 `Path` 객체로 생성된다.

| 타입 | Path 클래스 | 주요 메서드 |
|------|-------------|------------|
| String | `StringPath` | `eq()`, `contains()`, `startsWith()`, `endsWith()` |
| 숫자 | `NumberPath` | `eq()`, `gt()`, `lt()`, `goe()`, `loe()`, `between()` |
| Boolean | `BooleanPath` | `isTrue()`, `isFalse()` |
| Enum | `EnumPath` | `eq()`, `ne()` |
| 날짜/시간 | `DateTimePath` | `before()`, `after()`, `between()` |

---

## 사용 방법

**방법 1. static import (권장)**

```java
import static com.example.domain.product.entity.QProduct.product;

queryFactory.selectFrom(product).fetch();
// → SELECT * FROM products;
```

**방법 2. 직접 인스턴스 생성**

같은 엔티티를 조인할 때(셀프 조인) 별칭이 필요한 경우 사용한다.

```java
QProduct product1 = new QProduct("p1");
QProduct product2 = new QProduct("p2");

queryFactory.selectFrom(product1)
    .join(product2).on(product1.category.eq(product2.category))
    .fetch();
```

---

## 주의사항

- Q클래스는 **자동 생성 파일**이므로 `.gitignore`에 추가해야 한다.
- 엔티티를 수정하면 Q클래스도 **재생성** 해야 한다. (`./gradlew compileJava`)
- Q클래스를 직접 수정하면 안 된다. (다음 빌드 시 덮어씌워짐)

```gitignore
# .gitignore
build/generated/
```