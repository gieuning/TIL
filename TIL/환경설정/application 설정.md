<details>
<summary>yml 설정</summary>

```text
server:
  port: 9090

spring:
  thymeleaf:
      prefix: classpath:/templates/ # thymeleaf 템플릿 파일의 경로를 지정
      suffix: .html # 템플릿 파일의 확장자를 .html로 설정
      cache: false # 템플릿 캐싱을 비활성화(개발 중에 템플릿 변경 사항을 즉시 반영)

  datasource:
    url: jdbc:oracle:thin:@//127.0.0.1:1521/xe
    hikari: # 기본적으로 사용되는 커넥션 풀 라이브러리인 HikariCP의 사용자명과 비밀번호 설정
      username: sky
      password: java$!

  jpa:
    hibernate:
      ddl-auto: none # DDL 쿼리 자동 생성 방지 (기존에 테이블이 존재하지 않으면 애플리케이션 실행 시 에러 발생)
    properties:
      hibernate:
        '[show_sql]' : true # 모든 SQL 문을 콘솔에 출력 ('' 와 []를 이용하면 문자 가능)
        '[format_sql]' : true # 쿼리를 가독성 있게 출력
        '[use_sql_comments]' : true # 디버깅용이하도록 추가정보 출력
```
</details>
