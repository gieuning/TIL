## build.gradle 설정
```text
// QueryDSL 라이브러리 (Spring Boot 3.x는 반드시 jakarta 버전)
implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'

// Q클래스 자동 생성을 위한 어노테이션 프로세서
annotationProcessor "com.querydsl:querydsl-apt:5.0.0:jakarta"
annotationProcessor "jakarta.annotation:jakarta.annotation-api"
annotationProcessor "jakarta.persistence:jakarta.persistence-api"

// Q클래스 생성 경로 지정
def querydslDir = layout.buildDirectory.dir("generated/querydsl").get().asFile

// tasks를 통해서 만들어진다. (Q파일들)
tasks.withType(JavaCompile).configureEach {
    options.getGeneratedSourceOutputDirectory().set(file(querydslDir))
}

// clean 시 Q클래스도 같이 삭제
clean {
  delete querydslDir
}
```
>▎Spring Boot 3.x부터 javax → jakarta로 바뀌었기 때문에 :jakarta 를 반드시 붙여야 한다.

---

## JPAQueryFactory Bean 등록
```java
@Configuration
public class QueryDslConfig {

  @PersistenceContext
  private EntityManager entityManager;

  @Bean
  public JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(entityManager);
  }
}
```
- EntityManager를 주입받아 JPAQueryFactory를 Bean으로 등록한다.
- 이후 Repository에서 @RequiredArgsConstructor로 주입받아 사용한다.

---


