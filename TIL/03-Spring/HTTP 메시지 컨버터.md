## HTTP 메시지 컨버터의 선택
- Spring MVC는 클라이언트의 `HTTP Accept` 헤더와 서버에서 반환하는 컨트롤러의 반환 타입 정보를 기반으로 적절한
`HttpMessageConverter`를 선택
- 메시지 컨버터는 인터페이스 기반으로 동작하며, 각각의 구현체가 특정 클래스 타입과 미디어 타입을 지원하도록 설계 

## HTTP 메시지 컨버터의 사용 시점
1. HTTP 요청 처리 (`@RequestBody`, `HttpEntity(RequestEntity)`)
- 컨트롤러 메서드의 파라미터에 @RequestBody 또는 HttpEntity (또는 그 하위인 RequestEntity)가 사용된 경우:
  - 스프링은 먼저 canRead() 메서드를 호출해 해당 메시지 컨버터가 대상 클래스 타입과 HTTP 요청의 Content-Type을 지원하는지 확인
  - 조건에 맞으면 read() 메서드를 호출해서 HTTP 메시지 본문을 읽고, 해당 타입의 객체로 변환
2. HTTP 응답 생성 (`@ResponseBody`, `HttpEntity(ResponseBody)`)
- 컨트롤러 메서드에서 @ResponseBody 또는 HttpEntity (또는 그 하위인 ResponseEntity)를 사용하여 값을 반환하는 경우:
  - 스프링은 반환 타입과 HTTP 요청의 Accept 헤더(혹은 @RequestMapping의 produces 옵션)를 확인하기 위해 canWrite() 메서드를 호출
  - 조건에 만족하면 write() 메서드를 호출하여 객체를 HTTP 응답 메시지로 변환

- `canRead()`, `canWrite()` : 메시지 컨버터가 해당 클래스, 미디어타입을 지원하는지 체크
- `read()`, `write()`: 메시지 컨버터를 통해서 메시지를 읽고 쓰는 기능

## 주요 메시지 컨버터 종류 (Spring Boot 기본 제공)
- 0 = ByteArrayHttpMessageConverter (클래스 타입: byte[], 미디어 타입: */*) 
- 1 = StringHttpMessageConverter (클래스 타입: String, 미디어 타입: */*
- 2 = MappingJackson2HttpMessageConverter (클래스 타입 : 객체 또는 HashMap, 미디어타입: application/json 관련

스프링 부투는 다양한 메시지 컨버터를 제공하는데, 대상 클래스 타입과 미디어 타입 둘을 체크해서 사용여부를 결정

**HTTP 요청 데이터 읽기**
- HTTP 요청이 오고 컨트롤러에서 `@RequestBody`, `HttpEntity` 파라미터를 사용한다.
- 메시지 컨버터가 메시지를 읽을 수 있는지 확인하기 위해 `canRead()`를 호출한다.
  - 대상 클래스 타입을 지원하는가.
    - 예) `@RequestBody`의 대상 클래스 (`byte[]`, `String`, `HelloData`)
  - HTTP 요청의 Content-Type 미디어 타입을 지원하는가
    - 예) `text/plain`, `application/json`, `*/*`
- `canRead()` 조건을 만족하면 `read()`를 호출해서 객체 생성하고, 반환한다.

**HTTP 응답 데이터 생성**
- HTTP 요청이 오고 컨트롤러에서 `@ResponseBody`, `HttpEntity`로 값이 반환된다.
- 메시지 컨버터가 메시지를 읽을 수 있는지 확인하기 위해 `canWrite()`를 호출한다.
    - 대상 클래스 타입을 지원하는가.
        - 예) `return`의 대상 클래스 (`byte[]`, `String`, `HelloData`)
    - HTTP 요청의 Accept 미디어 타입을 지원하는가.(더 정확히는 `@RequestMapping`의 `produces`)
        - 예) `text/plain`, `application/json`, `*/*`
- `canWrite()` 조건을 만족하면 `write ()`를 호출해서 객체 생성하고, 반환한다.

**StringHttpMessageConverter**
예시) content-type:application/json 일때
```java
@RequestMapping
void hello(@RequestBody String data) {}
```
- HTTP 메시지 컨버터가 `canRead()`를 호출해서 대상 클래스 타입과 미디어 타입 둘을 체크해서 사용 여부를 결정
- 해당 예시 코드는 String으로 되어 있기 때문에 ByteArrayHttpMessageConverter 은 패스하고, 다음 우선순위를 가지는
StringHttpMessageConverter 로 가게 되고 클래스 타입이 일치하며 미디어 타입은 모두 받을 수 있기 때문에 StringHttpMessageConverter로 read()된다.

**MappingJackson2HttpMessageConverter**
예시) content-type:application/json 일때
```java
@RequestMapping
void hello(@RequestBody HelloData data) {}
```

--- 
## 정리
요청과 응답 처리에 공통적으로 사용됨: <br>
canRead()/read()는 요청 데이터를 읽을 때, canWrite()/write()는 응답 데이터를 생성할 때 사용. <br>
 
동적 컨버터 선택: <br>
HTTP 요청의 Content-Type과 Accept 헤더, 대상 데이터의 타입 등을 종합적으로 고려하여 알맞은 메시지 컨버터를 선택. <br>

스프링 부트의 기본 메시지 컨버터: <br>
ByteArray, String, JSON 변환용 컨버터들을 기본적으로 제공하며, 필요에 따라 커스터마이징하거나 추가할 수 있다.  <br>

이와 같이, 스프링 MVC (또는 스프링 부트)는 메시지 컨버터를 통해 HTTP 요청/응답의 본문 데이터를 객체로 변환하거나 객체를 HTTP 응답 본문으로 변환하는 과정을 수행한다.