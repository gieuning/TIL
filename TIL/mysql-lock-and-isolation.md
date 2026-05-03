# MySQL InnoDB Lock & Isolation 정리

## 1. 왜 락과 격리 수준을 알아야 할까?

DB는 여러 트랜잭션이 동시에 같은 데이터를 읽고 쓰는 환경이다.
이때 아무 제어가 없으면 다음과 같은 문제가 생긴다.

- 동시에 같은 데이터를 수정해서 값이 꼬일 수 있다.
- 한 트랜잭션이 아직 커밋하지 않은 데이터를 다른 트랜잭션이 읽을 수 있다.
- 같은 트랜잭션 안에서 같은 쿼리를 두 번 실행했는데 결과가 달라질 수 있다.

이 문제를 제어하기 위해 DB는 크게 두 가지 방식을 사용한다.

- **Lock**: 다른 트랜잭션의 접근을 막거나 기다리게 한다.
- **MVCC**: 락을 최소화하면서 과거 버전의 데이터를 읽게 한다.

MySQL의 InnoDB는 Lock과 MVCC를 함께 사용한다.

---

## 2. 기본 락: S Lock과 X Lock

### S Lock, Shared Lock, 공유 락

S Lock은 읽기용 락이다.

어떤 트랜잭션이 특정 레코드에 S Lock을 가지고 있어도, 다른 트랜잭션은 같은 레코드에 S Lock을 얻을 수 있다.
즉, 여러 트랜잭션이 동시에 읽는 것은 허용된다.

하지만 다른 트랜잭션이 X Lock을 얻으려고 하면 대기해야 한다.

### X Lock, Exclusive Lock, 배타 락

X Lock은 쓰기용 락이다.

어떤 트랜잭션이 특정 레코드에 X Lock을 가지고 있으면, 다른 트랜잭션은 같은 레코드에 S Lock도 X Lock도 얻을 수 없다.

즉, X Lock은 읽기 락과 쓰기 락 모두와 충돌한다.

### 락 호환성

| 현재 보유 락 | 다른 트랜잭션의 S Lock 요청 | 다른 트랜잭션의 X Lock 요청 |
| --- | --- | --- |
| S Lock | 가능 | 대기 |
| X Lock | 대기 | 대기 |

면접에서는 이렇게 말하면 좋다.

> 공유 락은 다른 공유 락과 호환되지만, 배타 락은 공유 락과 배타 락 모두와 충돌합니다.

---

## 3. InnoDB의 주요 락 종류

InnoDB는 단순히 S Lock, X Lock만 사용하는 것이 아니라 인덱스를 기준으로 여러 종류의 락을 사용한다.

### Record Lock

Record Lock은 인덱스 레코드 자체에 거는 락이다.

예를 들어 `id = 10`인 row를 수정하면 InnoDB는 해당 인덱스 레코드에 락을 건다.

```sql
UPDATE member
SET name = 'kim'
WHERE id = 10;
```

이 경우 `id = 10` 레코드에 X Lock이 걸린다.

중요한 점은 InnoDB의 row lock은 실제로는 **인덱스 레코드에 걸린다**는 것이다.
조건에 적절한 인덱스가 없으면 더 많은 범위가 잠기거나 성능 문제가 생길 수 있다.

### Gap Lock

Gap Lock은 실제 레코드가 아니라 **인덱스 레코드 사이의 빈 공간**에 거는 락이다.

예를 들어 `id`가 10, 20인 데이터만 있다고 해보자.

```text
10 ---- gap ---- 20
```

이때 10과 20 사이의 공간이 gap이다.
Gap Lock은 이 공간에 새로운 데이터가 INSERT되는 것을 막기 위해 사용된다.

예를 들어 다른 트랜잭션이 `id = 15`를 INSERT하지 못하게 막을 수 있다.

### Next-Key Lock

Next-Key Lock은 **Record Lock + Gap Lock**이다.

즉, 특정 인덱스 레코드와 그 앞의 gap을 함께 잠그는 방식이다.

InnoDB는 Repeatable Read에서 범위 검색을 할 때 팬텀 리드를 막기 위해 Next-Key Lock을 사용할 수 있다.

```sql
SELECT *
FROM member
WHERE id BETWEEN 10 AND 20
FOR UPDATE;
```

이 쿼리는 단순히 `id = 10`, `id = 20` 레코드만 잠그는 것이 아니라, 조건 범위에 해당하는 gap도 잠글 수 있다.
그래야 다른 트랜잭션이 `id = 15`를 새로 INSERT해서 결과 집합을 바꾸는 것을 막을 수 있다.

### Insert Intention Lock

Insert Intention Lock은 INSERT를 하기 전에 거는 의도 락이다.

여러 트랜잭션이 서로 다른 위치에 INSERT하려는 경우에는 동시에 진행될 수 있다.
하지만 Gap Lock이 잡혀 있는 범위에 INSERT하려고 하면 대기할 수 있다.

### Intention Lock

Intention Lock은 테이블 레벨에서 사용되는 락이다.

InnoDB는 row lock을 걸기 전에 테이블에 의도 락을 표시한다.
이는 "이 트랜잭션이 이 테이블의 특정 row에 락을 걸 예정"이라는 신호에 가깝다.

대표적으로 다음이 있다.

- **IS Lock**: Intention Shared Lock
- **IX Lock**: Intention Exclusive Lock

일반적인 백엔드 개발 면접에서는 Intention Lock보다 Record Lock, Gap Lock, Next-Key Lock을 더 자주 물어본다.

---

## 4. MVCC란?

MVCC는 Multi-Version Concurrency Control의 약자다.

한국어로는 다중 버전 동시성 제어라고 한다.

핵심 아이디어는 간단하다.

> 데이터를 수정할 때 기존 데이터를 바로 덮어쓰기만 하는 것이 아니라, 이전 버전을 남겨두고 다른 트랜잭션이 필요하면 그 이전 버전을 읽게 하는 방식이다.

InnoDB는 undo log를 이용해 이전 버전의 데이터를 관리한다.

덕분에 일반 SELECT는 보통 락을 걸지 않고도 일관된 데이터를 읽을 수 있다.

```sql
SELECT *
FROM member
WHERE id = 1;
```

이런 일반 SELECT는 보통 **Consistent Read**로 동작한다.
즉, 현재 최신 데이터가 아니라 트랜잭션의 격리 수준에 맞는 스냅샷을 읽는다.

---

## 5. Consistent Read와 Current Read

InnoDB를 이해할 때는 일반 SELECT와 락을 거는 읽기를 구분해야 한다.

### Consistent Read

Consistent Read는 MVCC 스냅샷을 읽는 방식이다.

일반 SELECT가 대표적이다.

```sql
SELECT *
FROM member
WHERE id = 1;
```

Repeatable Read에서는 같은 트랜잭션 안에서 같은 스냅샷을 계속 읽는다.

### Current Read

Current Read는 최신 데이터를 읽고 필요한 경우 락을 거는 방식이다.

다음 쿼리들이 대표적이다.

```sql
SELECT *
FROM member
WHERE id = 1
FOR UPDATE;

SELECT *
FROM member
WHERE id = 1
LOCK IN SHARE MODE;

UPDATE member
SET name = 'lee'
WHERE id = 1;

DELETE FROM member
WHERE id = 1;
```

Current Read는 실제 최신 데이터를 기준으로 동작해야 하므로 락과 강하게 연결된다.

정리하면 다음과 같다.

| 읽기 방식 | 대표 쿼리 | 특징 |
| --- | --- | --- |
| Consistent Read | 일반 SELECT | MVCC 스냅샷 읽기 |
| Current Read | SELECT FOR UPDATE, UPDATE, DELETE | 최신 데이터 읽기, 락 사용 |

---

## 6. 트랜잭션 격리 수준

트랜잭션 격리 수준은 동시에 실행되는 트랜잭션들이 서로에게 얼마나 영향을 줄 수 있는지를 정하는 기준이다.

SQL 표준 격리 수준은 다음 네 가지다.

1. Read Uncommitted
2. Read Committed
3. Repeatable Read
4. Serializable

아래로 갈수록 격리성이 강해지고, 일반적으로 동시성은 낮아진다.

---

## 7. 이상 현상

격리 수준을 이해하려면 먼저 이상 현상을 알아야 한다.

### Dirty Read

Dirty Read는 다른 트랜잭션이 아직 커밋하지 않은 데이터를 읽는 현상이다.

예를 들어 A 트랜잭션이 데이터를 수정했지만 아직 커밋하지 않았다.
그런데 B 트랜잭션이 그 수정된 값을 읽었다.
이후 A 트랜잭션이 롤백하면 B가 읽은 값은 실제로 존재하지 않는 값이 된다.

### Non-Repeatable Read

Non-Repeatable Read는 같은 트랜잭션 안에서 같은 row를 두 번 읽었는데 값이 달라지는 현상이다.

예를 들어 B 트랜잭션이 `id = 1` 회원의 이름을 읽었다.
그 사이 A 트랜잭션이 해당 회원의 이름을 수정하고 커밋했다.
B 트랜잭션이 다시 `id = 1`을 읽으면 이전과 다른 값이 보일 수 있다.

### Phantom Read

Phantom Read는 같은 트랜잭션 안에서 같은 조건으로 범위 조회를 두 번 했는데, 없던 row가 생기거나 있던 row가 사라진 것처럼 보이는 현상이다.

예를 들어 B 트랜잭션이 `age >= 20`인 회원을 조회했다.
그 사이 A 트랜잭션이 `age = 25`인 회원을 INSERT하고 커밋했다.
B 트랜잭션이 다시 `age >= 20` 조건으로 조회했을 때 새로운 row가 보이면 Phantom Read다.

---

## 8. 격리 수준별 특징

### Read Uncommitted

커밋되지 않은 데이터도 읽을 수 있다.

Dirty Read가 발생할 수 있으므로 실무에서는 거의 사용하지 않는다.

### Read Committed

커밋된 데이터만 읽을 수 있다.

Dirty Read는 방지한다.
하지만 같은 트랜잭션 안에서도 SELECT를 실행할 때마다 최신 커밋 데이터를 다시 읽기 때문에 Non-Repeatable Read가 발생할 수 있다.

Oracle, PostgreSQL의 기본 격리 수준이 Read Committed다.

### Repeatable Read

같은 트랜잭션 안에서 같은 데이터를 반복해서 읽어도 같은 결과를 보장하는 격리 수준이다.

MySQL InnoDB의 기본 격리 수준이다.

InnoDB의 Repeatable Read에서는 일반 SELECT가 MVCC 스냅샷을 읽는다.
따라서 같은 트랜잭션 안에서 같은 SELECT를 반복하면 같은 스냅샷을 보기 때문에 결과가 유지된다.

또한 락을 거는 읽기나 범위 변경에서는 Gap Lock, Next-Key Lock을 사용해 다른 트랜잭션의 INSERT를 막을 수 있다.

### Serializable

가장 강한 격리 수준이다.

트랜잭션이 마치 순서대로 하나씩 실행되는 것처럼 동작하도록 만든다.
동시성은 가장 낮고, 락 대기나 데드락 가능성이 커질 수 있다.

---

## 9. 격리 수준과 이상 현상 정리

일반적인 SQL 표준 기준으로 보면 다음과 같다.

| 격리 수준 | Dirty Read | Non-Repeatable Read | Phantom Read |
| --- | --- | --- | --- |
| Read Uncommitted | 발생 가능 | 발생 가능 | 발생 가능 |
| Read Committed | 방지 | 발생 가능 | 발생 가능 |
| Repeatable Read | 방지 | 방지 | 발생 가능 |
| Serializable | 방지 | 방지 | 방지 |

하지만 MySQL InnoDB는 구현 방식 때문에 주의해서 말해야 한다.

MySQL InnoDB의 Repeatable Read에서는 일반 SELECT는 MVCC 스냅샷을 사용한다.
따라서 일반적인 반복 조회에서는 Phantom Read가 잘 발생하지 않는다.

또한 `SELECT ... FOR UPDATE`, `UPDATE`, `DELETE` 같은 Current Read에서는 Next-Key Lock을 사용해 범위에 새 row가 끼어드는 것을 막을 수 있다.

즉, 면접에서는 이렇게 말하는 것이 정확하다.

> SQL 표준 기준으로 Repeatable Read는 Phantom Read를 완전히 막는 수준은 아니지만, MySQL InnoDB의 Repeatable Read는 MVCC와 Next-Key Lock을 통해 많은 경우 Phantom Read를 방지합니다.

---

## 10. MySQL InnoDB Repeatable Read에서 팬텀 리드 설명하기

가장 헷갈리는 부분이다.

다음 문장은 반만 맞다.

> MySQL Repeatable Read는 MVCC를 쓰기 때문에 팬텀 리드를 막는다.

더 정확히는 다음과 같이 말해야 한다.

> MySQL InnoDB의 Repeatable Read에서 일반 SELECT는 MVCC 스냅샷을 읽기 때문에 같은 트랜잭션 안에서 같은 결과를 보장합니다. 그리고 SELECT FOR UPDATE 같은 잠금 읽기나 UPDATE, DELETE에서는 Next-Key Lock을 사용해 범위 내 INSERT를 막아 팬텀 리드를 방지합니다.

핵심은 일반 SELECT와 잠금 읽기를 구분하는 것이다.

### 일반 SELECT

```sql
START TRANSACTION;

SELECT *
FROM member
WHERE age >= 20;

-- 다른 트랜잭션에서 age = 25인 회원을 INSERT 후 COMMIT

SELECT *
FROM member
WHERE age >= 20;

COMMIT;
```

Repeatable Read에서는 두 번째 SELECT도 처음과 같은 스냅샷을 읽는다.
따라서 새로 INSERT된 row가 보이지 않을 수 있다.

이것은 MVCC의 Consistent Read 덕분이다.

### SELECT FOR UPDATE

```sql
START TRANSACTION;

SELECT *
FROM member
WHERE age >= 20
FOR UPDATE;

-- 다른 트랜잭션에서 age = 25인 회원을 INSERT하려고 하면 대기할 수 있음

COMMIT;
```

이 경우에는 스냅샷을 읽는 것이 아니라 최신 데이터를 읽고 락을 건다.
범위 조건에 인덱스가 적절히 사용되면 Next-Key Lock이 걸려서 다른 트랜잭션의 INSERT를 막을 수 있다.

---

## 11. 인덱스와 락의 관계

InnoDB의 row lock은 인덱스를 기반으로 동작한다.

따라서 조건에 인덱스가 있느냐 없느냐가 매우 중요하다.

```sql
UPDATE member
SET status = 'ACTIVE'
WHERE email = 'a@test.com';
```

`email`에 인덱스가 있다면 해당 인덱스 레코드를 빠르게 찾아 필요한 row에 락을 걸 수 있다.

하지만 `email`에 인덱스가 없다면 더 많은 row를 스캔해야 하고, 그 과정에서 불필요하게 많은 락이 걸릴 수 있다.

실무에서는 다음을 기억하면 좋다.

- UPDATE, DELETE 조건 컬럼에는 인덱스가 중요하다.
- 범위 조건에서는 Gap Lock, Next-Key Lock이 걸릴 수 있다.
- 인덱스를 잘못 타면 예상보다 넓은 범위가 잠길 수 있다.

---

## 12. 데드락

데드락은 두 개 이상의 트랜잭션이 서로가 가진 락을 기다리면서 더 이상 진행하지 못하는 상태다.

예를 들어 다음 상황을 보자.

1. 트랜잭션 A가 `member_id = 1` row를 잠근다.
2. 트랜잭션 B가 `member_id = 2` row를 잠근다.
3. 트랜잭션 A가 `member_id = 2` row를 수정하려고 한다.
4. 트랜잭션 B가 `member_id = 1` row를 수정하려고 한다.

서로 상대방이 가진 락을 기다리기 때문에 데드락이 발생한다.

InnoDB는 데드락을 감지하면 둘 중 하나의 트랜잭션을 롤백시킨다.

실무에서 데드락을 줄이는 방법은 다음과 같다.

- 여러 row를 수정할 때 항상 같은 순서로 접근한다.
- 트랜잭션을 짧게 유지한다.
- 불필요한 범위 락을 피한다.
- UPDATE, DELETE 조건에 적절한 인덱스를 둔다.
- 실패 시 재시도 로직을 고려한다.

---

## 13. 스프링에서 연결되는 포인트

스프링에서 `@Transactional`을 사용하면 트랜잭션 경계를 만들 수 있다.

```java
@Transactional
public void changeName(Long memberId, String name) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow();

    member.changeName(name);
}
```

이 코드에서 JPA의 변경 감지로 UPDATE가 발생하면 DB에서는 해당 row에 X Lock이 걸릴 수 있다.

격리 수준은 다음처럼 지정할 수 있다.

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void businessLogic() {
    // ...
}
```

다만 실무에서는 DB 기본 격리 수준을 그대로 사용하는 경우가 많다.
MySQL InnoDB라면 기본값은 Repeatable Read다.

JPA에서 비관적 락을 명시적으로 사용할 수도 있다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select m from Member m where m.id = :id")
Optional<Member> findByIdForUpdate(@Param("id") Long id);
```

이는 DB의 `SELECT ... FOR UPDATE`와 비슷하게 동작한다.

---

## 14. 면접 답변 템플릿

### Q. S Lock과 X Lock의 차이는 무엇인가요?

> S Lock은 공유 락으로, 다른 트랜잭션의 S Lock과는 호환됩니다. 즉 여러 트랜잭션이 동시에 읽을 수 있습니다. 반면 X Lock은 배타 락으로, 다른 트랜잭션의 S Lock과 X Lock 모두와 충돌합니다. 그래서 특정 트랜잭션이 X Lock을 잡고 있으면 다른 트랜잭션은 같은 데이터에 읽기 락이나 쓰기 락을 얻기 어렵습니다.

### Q. Gap Lock은 무엇인가요?

> Gap Lock은 실제 레코드가 아니라 인덱스 레코드 사이의 빈 공간에 거는 락입니다. 범위 조회나 범위 수정 중에 다른 트랜잭션이 그 범위 안에 새로운 row를 INSERT하는 것을 막기 위해 사용됩니다. InnoDB는 Repeatable Read에서 팬텀 리드를 방지하기 위해 Gap Lock이나 Next-Key Lock을 사용할 수 있습니다.

### Q. Next-Key Lock은 무엇인가요?

> Next-Key Lock은 Record Lock과 Gap Lock을 합친 락입니다. 특정 인덱스 레코드와 그 앞의 gap을 함께 잠급니다. 범위 조건에서 다른 트랜잭션이 새로운 row를 끼워 넣어 조회 결과가 바뀌는 것을 막는 데 사용됩니다.

### Q. MVCC는 무엇인가요?

> MVCC는 다중 버전 동시성 제어입니다. 데이터를 수정할 때 이전 버전을 undo log에 보관하고, 트랜잭션은 격리 수준에 맞는 스냅샷을 읽습니다. 덕분에 일반 SELECT는 락을 걸지 않고도 일관된 읽기를 할 수 있고, 읽기와 쓰기 간의 충돌을 줄일 수 있습니다.

### Q. MySQL InnoDB의 Repeatable Read는 팬텀 리드를 막나요?

> SQL 표준 기준으로 Repeatable Read는 Phantom Read를 완전히 막는 격리 수준은 아닙니다. 하지만 MySQL InnoDB의 Repeatable Read에서는 일반 SELECT는 MVCC 스냅샷을 읽기 때문에 같은 트랜잭션 안에서 같은 결과를 보장합니다. 또한 SELECT FOR UPDATE 같은 잠금 읽기나 UPDATE, DELETE에서는 Next-Key Lock을 사용해 범위 내 INSERT를 막기 때문에 많은 경우 팬텀 리드를 방지할 수 있습니다.

### Q. 일반 SELECT와 SELECT FOR UPDATE의 차이는 무엇인가요?

> 일반 SELECT는 보통 MVCC 기반의 Consistent Read로 동작해서 스냅샷을 읽습니다. 반면 SELECT FOR UPDATE는 Current Read로 최신 데이터를 읽고 X Lock을 겁니다. 그래서 SELECT FOR UPDATE는 다른 트랜잭션의 수정이나 삽입을 막는 동시성 제어에 사용할 수 있습니다.

---

## 15. 최종 요약

- S Lock은 공유 락이고, S Lock끼리는 호환된다.
- X Lock은 배타 락이고, S Lock과 X Lock 모두와 충돌한다.
- InnoDB의 row lock은 인덱스 레코드를 기준으로 동작한다.
- Record Lock은 실제 인덱스 레코드에 거는 락이다.
- Gap Lock은 인덱스 레코드 사이 빈 공간에 거는 락이다.
- Next-Key Lock은 Record Lock과 Gap Lock을 합친 락이다.
- MVCC는 undo log를 이용해 과거 버전의 데이터를 읽게 하는 동시성 제어 방식이다.
- 일반 SELECT는 보통 Consistent Read로 동작하며 MVCC 스냅샷을 읽는다.
- SELECT FOR UPDATE, UPDATE, DELETE는 Current Read로 동작하며 락과 관련된다.
- MySQL InnoDB의 기본 격리 수준은 Repeatable Read다.
- InnoDB Repeatable Read에서는 일반 SELECT는 MVCC로, 잠금 읽기나 범위 변경은 Next-Key Lock으로 팬텀 리드를 방지할 수 있다.

