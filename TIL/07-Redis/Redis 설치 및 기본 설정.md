# Redis 설치 및 기본 설정

```bash
docker run -d --name redis-server -p 6379:6379 --restart always redis

```

**Redis CLI 접속**
```bash
docker exec -it redis-server redis-cli
```

**Redis 저장소에서 세션 데이터 확인**

```jsx
127.0.0.1:6379> KEYS *
1) "spring:session:sessions:<SESSION_ID>"
```

- `KEYS *` 명령어를 통해 등록 된 모든 key를 확인 할 있다.
- **세션 키 구조:** 세션은 `spring:session:sessions:` 접두사와 고유한 세션 ID로 구성.


## Redis-Spring Boot 설정

Gradle 의존성 추가:
```build
// Redis와 통신하기 위한 라이브러리
implementation 'org.springframework.session:spring-session-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

Redis 설정 (application.yml):
```yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # 비밀번호가 없다면 비워둠
      # timeout: 1s
      # database: 0
```