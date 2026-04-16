# Redis 캐싱 패턴 (Spring Boot)

## 개요

`CategoryService`에서 Redis를 활용한 3가지 캐싱 패턴을 학습했다.
캐시 키는 고정 상수(`"cacheCategories"`)로 관리하고, 데이터는 JSON 직렬화하여 String으로 저장한다.

---

## 1. Cache-Aside (Lazy Loading) 패턴

**"필요할 때만 캐시에 올린다"**

```java
public List<CategoryResponse> findAllForCacheAside() {
    String cachedCategories = redisTemplate.opsForValue().get(CACHE_KEY_CATEGORY);

    if (StringUtils.hasText(cachedCategories)) {
        return JsonUtil.fromJsonList(cachedCategories, CategoryResponse.class); // Cache HIT
    }

    // Cache MISS → DB 조회 후 캐시에 저장
    List<CategoryResponse> categories = findAll();
    
    if (!categories.isEmpty()) {
        redisTemplate.opsForValue().set(CACHE_KEY_CATEGORY, JsonUtil.toJson(categories), 1, TimeUnit.HOURS);
    }
    return categories;
}
```

### 흐름

```
요청 → Redis 확인
         ├─ HIT  → Redis 데이터 반환 (DB 안 탐)
         └─ MISS → DB 조회 → Redis에 저장 → 반환
```

### 특징

- 읽기 성능 최적화에 적합
- 첫 요청은 항상 Cache MISS (Cold Start)
- TTL(1시간) 설정으로 데이터 만료 관리

---

## 2. Write-Through 패턴

**"DB와 캐시를 항상 동시에 갱신한다"**

```java
public void saveWriteThrough(CategoryRequest request) {
    String cachedBackup = redisTemplate.opsForValue().get(CACHE_KEY_CATEGORY); // 롤백용 백업

    try {
        create(request);           // DB 저장
        updateCacheCategories();   // 캐시 즉시 갱신
    } catch (Exception e) {
        redisTemplate.opsForValue().set(CACHE_KEY_CATEGORY, cachedBackup); // 실패 시 롤백
        log.error("Failed to save {}", e.getMessage(), e);
    }
}
```

### 흐름

```
요청 → DB 저장 → 캐시 갱신 → 완료
              └─ 실패 시 → 이전 캐시로 롤백
```

### 특징

- 캐시와 DB가 항상 동기화 → 데이터 정합성 보장
- 쓰기 시마다 캐시도 갱신하므로 쓰기 비용이 증가
- 예외 시 백업해둔 캐시로 롤백하는 방어 로직 포함

---

## 3. Write-Back (Write-Behind) 패턴

**"캐시에 먼저 쓰고, DB는 나중에 비동기로 처리한다"**

```java
public void saveWriteBack(CategoryRequest request) {
    // 1. 캐시 먼저 업데이트
    List<CategoryResponse> categories = ...; // 기존 캐시 파싱
    categories.add(new CategoryResponse(request.getName(), ...));
    redisTemplate.opsForValue().set(CACHE_KEY_CATEGORY, JsonUtil.toJson(categories));

    // 2. DB는 비동기로 후처리
    saveToDatabaseAsync(request);
}

@Async
public void saveToDatabaseAsync(CategoryRequest request) {
    create(request); // DB 저장
}
```

### 흐름

```
요청 → 캐시 즉시 업데이트 → 응답 반환
                └─ @Async → DB 저장 (백그라운드)
```

### 특징

- 응답 속도가 가장 빠름 (DB I/O 대기 없음)
- DB 저장 실패 시 캐시와 DB 불일치 위험 존재
- **ID 전략 주의**: DB auto_increment는 DB를 거쳐야 생성되므로, Write-Back에서는 UUID 사용을 고려해야 한다
  - 또는 캐시 응답에서 ID 필드를 제외하는 방향으로 설계

---

## 패턴 비교 요약

| 패턴 | 쓰기 흐름 | 정합성 | 쓰기 속도 | 주요 리스크 |
|---|---|---|---|---|
| Cache-Aside | 읽기 전용 | 만료 전까지 stale 가능 | - | Cold Start |
| Write-Through | DB → 캐시 동기 | 높음 | 느림 | 쓰기 비용 증가 |
| Write-Back | 캐시 → DB 비동기 | 낮음 | 빠름 | DB 저장 실패 시 데이터 유실 |

---

## TTL (Time To Live)

### TTL이란?

TTL은 캐시 데이터의 **유효 시간**이다.
설정한 시간이 지나면 Redis에서 해당 키가 자동으로 삭제된다.

- TTL이 없으면 데이터가 영구 저장 → DB 변경사항이 캐시에 반영되지 않는 **stale 데이터** 문제 발생
- TTL을 짧게 설정할수록 정합성이 올라가지만, Cache MISS가 잦아져 DB 부하가 증가

### TTL 설정 방법

```java
// 저장 시 TTL 함께 설정 (1시간)
redisTemplate.opsForValue().set(KEY, value, 1, TimeUnit.HOURS);

// 이미 저장된 키에 TTL만 별도 설정
redisTemplate.expire(KEY, 30, TimeUnit.MINUTES);

// 현재 남은 TTL 조회
Long remainSeconds = redisTemplate.getExpire(KEY, TimeUnit.SECONDS);

// TTL 없이 저장 (영구 저장 — 주의)
redisTemplate.opsForValue().set(KEY, value);
```

### TimeUnit 옵션

| 단위 | 상수 |
|---|---|
| 초 | `TimeUnit.SECONDS` |
| 분 | `TimeUnit.MINUTES` |
| 시간 | `TimeUnit.HOURS` |
| 일 | `TimeUnit.DAYS` |

### TTL 설정 기준

| 데이터 성격 | 권장 TTL |
|---|---|
| 거의 안 바뀌는 데이터 (카테고리, 코드성 데이터) | 1시간 ~ 1일 |
| 자주 바뀌는 데이터 (재고, 가격) | 1분 ~ 10분 |
| 실시간성이 중요한 데이터 | TTL 대신 이벤트 기반 캐시 삭제 |

---

## 패턴 선택 가이드

### Cache-Aside 를 선택할 때

- **읽기가 압도적으로 많은** 서비스 (조회 API)
- 데이터가 자주 바뀌지 않는 경우 (카테고리, 공지사항 등)
- 캐시 구현을 단순하게 유지하고 싶을 때
- 일시적인 stale 데이터가 허용되는 경우

```
예시: 카테고리 목록, 상품 상세, 공지사항
```

### Write-Through 를 선택할 때

- 쓰기 직후 조회가 바로 발생하는 경우
- 캐시와 DB의 **정합성이 중요**한 경우
- 쓰기 빈도가 낮고 읽기 빈도가 높은 경우

```
예시: 사용자 프로필, 설정 데이터
```

### Write-Back 을 선택할 때

- **쓰기 성능이 최우선**인 경우
- 일시적인 DB 저장 지연이 허용되는 경우
- 트래픽이 폭발적으로 몰리는 상황 (이벤트, 배치 처리 등)
- ID를 UUID로 관리하거나, 응답에 ID가 불필요한 경우

```
예시: 좋아요 수, 조회수, 로그성 데이터
```

### 패턴 선택 요약

```
정합성 중요  →  Write-Through
속도 중요    →  Write-Back
읽기 위주    →  Cache-Aside
```

---

## 핵심 메모

- `redisTemplate.opsForValue()` — Redis의 **String(Key-Value)** 자료구조를 다루는 API
- 캐시 데이터는 직렬화(JSON 변환) 후 저장, 역직렬화 후 사용
- **Around 패턴**: 쓰기 시 캐시를 삭제하는 방식도 존재 (`redisTemplate.delete(key)`) — 갱신 주기가 길면 유효한 전략