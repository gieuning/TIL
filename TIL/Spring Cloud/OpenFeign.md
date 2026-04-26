# OpenFeign + Spring Retry

## OpenFeign

외부 API 호출을 자바 인터페이스처럼 선언하는 HTTP 클라이언트.  
RestTemplate/WebClient보다 선언형이라 코드가 단순하다.

### 활성화

```java
@SpringBootApplication
@EnableFeignClients
@EnableRetry
public class Application { ... }
```

### 클라이언트 선언

```java
@FeignClient(
    name = "external-product",
    url = "${external.external-shop.url}",
    configuration = OpenFeignConfig.class
)
public interface ExternalShopClient {

    @GetMapping("/products")
    ExternalProductResponse getProducts(
        @RequestParam("page") Integer page,
        @RequestParam("size") Integer size
    );
}
```

### 타임아웃 설정

```java
@Bean
public Request.Options feignOptions() {
    return new Request.Options(
        10000, TimeUnit.MILLISECONDS,  // 연결 타임아웃
        60000, TimeUnit.MILLISECONDS,  // 읽기 타임아웃
        true
    );
}
```

---

## Feign Retry vs Spring Retry

Feign은 자체 재시도 기능을 가지고 있지만, 서비스 레이어에서 `@Retryable`로 통제하는 방식이 더 명확하다.

| | Feign Retry | Spring Retry (`@Retryable`) |
|---|---|---|
| 위치 | HTTP 클라이언트 내부 | 서비스 메서드 |
| 재시도 대상 | HTTP 오류 | 지정한 예외 |
| 추적/분기 | 어려움 | 명확 |

### Feign Retry 비활성화

```java
@Bean
public Retryer feignRetryer() {
    return Retryer.NEVER_RETRY;
}
```

호출 실패 시 Feign이 바로 예외를 던지고, 재시도 여부는 서비스에서 판단한다.

---

## Spring Retry

### 전체 흐름

```
서비스 메서드 호출
  → externalShopClient.getProducts()  [Feign: HTTP 요청]
  → 실패 시 Feign은 재시도 없이 예외 던짐
  → catch → DomainException으로 변환
  → @Retryable이 DomainException 감지 → 메서드 전체 재실행
  → 최대 3번, 1초 간격
```

### 구현

```java
@Retryable(value = DomainException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
@Transactional
public void save() {
    ExternalProductResponse response;
    try {
        response = externalShopClient.getProducts(1, 10);
    } catch (Exception e) {
        throw new DomainException(DomainExceptionCode.NOT_FOUND_PRODUCT);
    }
    // ...
}
```

### @Recover — 최종 실패 처리

```java
@Recover
public void recover(DomainException e) {
    // 3번 모두 실패했을 때 실행
    log.error("외부 API 호출 최종 실패", e);
}
```

---

## Retry 대상 예외 선정

재시도가 의미 있는 경우와 없는 경우를 구분해야 한다.

| 재시도 의미 있음 | 재시도 의미 없음 |
|---|---|
| 네트워크 일시 장애 | 데이터 없음 (`NOT_FOUND`) |
| 외부 시스템 일시 오류 | 유효성 검증 실패 |
| 타임아웃 | 권한 없음 |

> `NOT_FOUND` 같은 예외는 재시도해도 결과가 바뀌지 않는다.  
> Retry 대상 예외는 "잠깐 후 다시 시도하면 성공 가능성이 있는 것"만 넣는다.