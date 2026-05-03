# Spring Session + Redis

## 동작 원리

의존성 2개만 추가하면 Spring이 기본 세션 저장소(서버 메모리)를 자동으로 Redis로 교체한다.

```gradle
implementation 'org.springframework.session:spring-session-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

코드에서는 `HttpSession`을 그냥 쓰지만, **실제 저장/조회는 Redis에서** 일어난다.

```java
// 로그인 시 세션에 저장 → 내부적으로 Redis에 저장됨
httpSession.setAttribute("userId", loginResponse.getUserId());
httpSession.setAttribute("email", loginResponse.getEmail());
```

## 요청 흐름
```
클라이언트 요청
  → Spring이 쿠키에서 SESSION ID 추출
  → Redis에서 해당 SESSION ID로 데이터 조회
  → HttpSession 객체로 바인딩
  → 컨트롤러에서 사용
```

## Redis 저장 구조
```
spring:session:sessions:{세션ID}
  → sessionAttr:userId       = 123
  → sessionAttr:email        = "test@test.com"
  → lastAccessedTime         = ...
  → maxInactiveInterval      = ...
```

## 장점
- 서버를 재시작해도 세션이 유지된다.
- 서버가 여러 대여도 세션을 공유할 수 있다 (수평 확장 가능).