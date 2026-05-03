# Timestamped - 공통 시간 관리

## 왜 만들었나?
모든 엔티티(Product, Category 등)에 createdAt, updatedAt이 반복적으로 필요한데,
매번 중복 작성하지 않기 위해 공통 클래스로 분리해서 상속받아 사용한다.
---
## 코드
```java

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
// @EntityListeners(AuditingEntityListener.class) -> @CreatedDate 사용할 때 
public class Timestamped {

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

}

```

---

## 핵심 어노테이션 
| 어노테이션 | 출처 | 역할 | 없으면? |
|------------|------|------|---------|
| `@MappedSuperclass` | JPA | 테이블 없이 컬럼만 자식 엔티티에 상속 | 자식 엔티티에 createdAt, updatedAt 컬럼 안 생김 |
| `@EntityListeners(AuditingEntityListener.class)` | Spring Data | 저장/수정 이벤트 감지 | @CreatedDate, @LastModifiedDate 동작 안 함 |
| `@CreationTimestamp` | Hibernate | 최초 INSERT 시 현재 시간 자동 입력 | 직접 setCreatedAt() 해줘야 함 |
| `@UpdateTimestamp` | Hibernate | UPDATE 될 때마다 현재 시간으로 자동 갱신 | 수정해도 updatedAt 안 바뀜 |
| `@CreatedDate` | Spring Data | 최초 저장 시 현재 시간 자동 입력 | @EnableJpaAuditing + @EntityListeners 필요 |
| `@LastModifiedDate` | Spring Data | 수정 시 현재 시간 자동 갱신 | @EnableJpaAuditing + @EntityListeners 필요 |
| `@CreatedBy` | Spring Data | 최초 저장 시 생성자 자동 입력 | AuditorAware 구현체 필요 |
| `@LastModifiedBy` | Spring Data | 수정 시 수정자 자동 입력 | AuditorAware 구현체 필요 |
| `@Column(updatable = false)` | JPA | UPDATE 쿼리에서 해당 컬럼 제외 | createdAt이 수정 시 바뀔 수 있음 |
| `@Column(nullable = false)` | JPA | DB NOT NULL 제약조건 | null 허용됨 |

---

## 사용 방법
```java
  public class Product extends Timestamped {
      // createdAt, updatedAt 자동으로 가짐                                                                                                                                                  
  }   
```

---

## @CreationTimestamp vs @CreatedDate 차이

| | @CreationTimestamp | @CreatedDate |
|---|---|---|
| 출처 | Hibernate | Spring Data |
| 필요 조건 | 없음 | @EnableJpaAuditing 필요 |
| 동작 시점 | DB INSERT 시 | 영속성 컨텍스트 저장 시 |

---

### 주의

- JpaAuditingConfig에 @EnableJpaAuditing이 선언되어 있어야 동작함
- @CreationTimestamp와 @UpdateTimestamp는 Hibernate가 직접 처리
- Spring의 @CreatedDate, @LastModifiedDate와는 다른 방식 (Hibernate vs Spring Data)

---

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // 엔티티의 생성일/수정일/생성자/수정자를 자동으로 채워주는 기능
// @CreateTimestamp을 쓰면 사실 @EnableJpaAuditing 없이도 동작함 
public class JpaAuditingConfig {

  /*
    원래는 저장할 때마다 직접
    product.setCreateAd(LocalDateTime.now());

    Auditing을 쓰면 자동으로 처리

    @CreatedDate
    private LocalDateTime createdAt;  // INSERT 시 자동 세팅
    
    @LastModifiedDate
    private LocalDateTime updatedAt;  // UPDATE 시 자동 세팅
    
    동작 조건 3가지:
    1. @EnableJpaAuditing — Auditing 기능 ON (JpaAuditingConfig의 역할)
    2. @EntityListeners(AuditingEntityListener.class) — 엔티티에 리스너 등록
    3. @CreatedDate / @LastModifiedDate — 자동으로 채울 필드 지정
    
    @EntityListeners(AuditingEntityListener.class)
    엔티티 이벤트 리스너를 붙이는 어노테이션, AuditingEntityListener -> Spring이 제공하는 리스너


  */

}

```                  
---

## 테스트 시 주의
Mock 테스트에서 Timestamped 상속한 엔티티 빌더로 만들면
createdAt, updatedAt이 null로 나올 수 있음

```java
// 해결
Category.builder()
.id(1L)
.name("전자기기")
.build();
// createdAt = null → response에서 NPE 주의
```
---

## 그래서 @CreationTimestamp vs @CreatedDate 언제 쓰냐?

**@CreationTimestamp (Hibernate)**

``` java
// 순수 JPA/Hibernate 프로젝트
// Spring Data 의존성 최소화하고 싶을 때
// 설정 간단하게 가고 싶을 때
@CreationTimestamp
private LocalDateTime createdAt;
```

**@CreatedDate (Spring Data)**

```java
// 생성자/수정자도 같이 관리할 때
@CreatedDate
private LocalDateTime createdAt;

@LastModifiedDate
private LocalDateTime updatedAt;

@CreatedBy        // 누가 만들었는지
private String createdBy;

@LastModifiedBy   // 누가 수정했는지
private String modifiedBy;
```

---

## 핵심 차이
```
@CreationTimestamp  →  시간만 필요할 때, 설정 간단하게
@CreatedDate        →  시간 + 생성자/수정자까지 관리할 때

```

---

# 정리 

**@CreationTimestamp, @UpdateTimestamp만 쓴다면:**

필요한 것
```java
@MappedSuperclass
public class Timestamped {
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

불필요한 것
- @EntityListeners(AuditingEntityListener.class) → Hibernate가 직접 처리하니까 없어도 됨
- JpaAuditingConfig (@EnableJpaAuditing) → Spring Data JPA 기능이라 필요 없음

---

**반대로 @CreatedDate, @LastModifiedDate로 바꾸면:**
```java
@EntityListeners(AuditingEntityListener.class)  // 필수
public class Timestamped {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```
이때는 JpaAuditingConfig도 필수
